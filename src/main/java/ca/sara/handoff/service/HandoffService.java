package ca.sara.handoff.service;

import ca.sara.handoff.api.CreateHandoffRequest;
import ca.sara.handoff.api.HandoffEventResponse;
import ca.sara.handoff.api.HandoffResponse;
import ca.sara.handoff.api.MetricsResponse;
import ca.sara.handoff.api.UpdateStatusRequest;
import ca.sara.handoff.domain.Handoff;
import ca.sara.handoff.domain.HandoffEvent;
import ca.sara.handoff.domain.HandoffStatus;
import ca.sara.handoff.domain.Priority;
import ca.sara.handoff.domain.ShiftType;
import ca.sara.handoff.repository.HandoffEventRepository;
import ca.sara.handoff.repository.HandoffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class HandoffService {

    private final HandoffRepository handoffRepository;
    private final HandoffEventRepository eventRepository;

    public HandoffService(HandoffRepository handoffRepository, HandoffEventRepository eventRepository) {
        this.handoffRepository = handoffRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<HandoffResponse> findAll(HandoffStatus status, Priority priority,
                                         ShiftType shiftType, String query) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        return handoffRepository.search(status, priority, shiftType, normalizedQuery)
                .stream()
                .map(HandoffResponse::from)
                .toList();
    }

    @Transactional
    public HandoffResponse create(CreateHandoffRequest request) {
        Handoff handoff = new Handoff(
                request.title().trim(),
                request.details().trim(),
                request.area().trim(),
                request.shiftType(),
                request.priority(),
                request.owner().trim()
        );
        Handoff saved = handoffRepository.save(handoff);
        eventRepository.save(HandoffEvent.created(saved.getId()));
        return HandoffResponse.from(saved);
    }

    @Transactional
    public HandoffResponse updateStatus(Long id, UpdateStatusRequest request) {
        Handoff handoff = handoffRepository.findById(id)
                .orElseThrow(() -> new HandoffNotFoundException(id));
        HandoffStatus previousStatus = handoff.getStatus();

        try {
            handoff.moveTo(request.status());
        } catch (IllegalStateException error) {
            throw new InvalidStatusTransitionException(error.getMessage());
        }

        if (previousStatus != handoff.getStatus()) {
            String note = request.note() == null || request.note().isBlank()
                    ? "moved to " + handoff.getStatus().name().toLowerCase(Locale.ROOT)
                    : request.note().trim();
            eventRepository.save(HandoffEvent.statusChanged(
                    handoff.getId(), previousStatus, handoff.getStatus(), note
            ));
        }
        return HandoffResponse.from(handoff);
    }

    @Transactional(readOnly = true)
    public List<HandoffEventResponse> history(Long id) {
        if (!handoffRepository.existsById(id)) {
            throw new HandoffNotFoundException(id);
        }
        return eventRepository.findByHandoffIdOrderByOccurredAtAsc(id)
                .stream()
                .map(HandoffEventResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MetricsResponse metrics() {
        List<Handoff> handoffs = handoffRepository.findAll();
        long newItems = handoffs.stream().filter(item -> item.getStatus() == HandoffStatus.NEW).count();
        long acknowledged = handoffs.stream().filter(item -> item.getStatus() == HandoffStatus.ACKNOWLEDGED).count();
        long done = handoffs.stream().filter(item -> item.getStatus() == HandoffStatus.DONE).count();
        long highPriorityOpen = handoffs.stream()
                .filter(item -> item.getPriority() == Priority.HIGH && item.getStatus() != HandoffStatus.DONE)
                .count();
        Map<String, Long> byArea = handoffs.stream()
                .collect(Collectors.groupingBy(Handoff::getArea, TreeMap::new, Collectors.counting()));

        return new MetricsResponse(handoffs.size(), newItems, acknowledged, done, highPriorityOpen, byArea);
    }
}

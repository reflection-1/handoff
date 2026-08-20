package ca.sara.handoff.api;

import ca.sara.handoff.domain.HandoffStatus;
import ca.sara.handoff.domain.Priority;
import ca.sara.handoff.domain.ShiftType;
import ca.sara.handoff.service.HandoffService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/handoffs")
public class HandoffController {

    private final HandoffService handoffService;

    public HandoffController(HandoffService handoffService) {
        this.handoffService = handoffService;
    }

    @GetMapping
    public List<HandoffResponse> findAll(
            @RequestParam(required = false) HandoffStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) ShiftType shiftType,
            @RequestParam(required = false) String query
    ) {
        return handoffService.findAll(status, priority, shiftType, query);
    }

    @PostMapping
    public ResponseEntity<HandoffResponse> create(@Valid @RequestBody CreateHandoffRequest request) {
        HandoffResponse created = handoffService.create(request);
        return ResponseEntity.created(URI.create("/api/handoffs/" + created.id())).body(created);
    }

    @PatchMapping("/{id}/status")
    public HandoffResponse updateStatus(@PathVariable Long id,
                                        @Valid @RequestBody UpdateStatusRequest request) {
        return handoffService.updateStatus(id, request);
    }

    @GetMapping("/{id}/history")
    public List<HandoffEventResponse> history(@PathVariable Long id) {
        return handoffService.history(id);
    }
}

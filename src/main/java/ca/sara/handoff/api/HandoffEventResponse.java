package ca.sara.handoff.api;

import ca.sara.handoff.domain.EventType;
import ca.sara.handoff.domain.HandoffEvent;
import ca.sara.handoff.domain.HandoffStatus;

import java.time.Instant;

public record HandoffEventResponse(
        Long id,
        EventType eventType,
        HandoffStatus fromStatus,
        HandoffStatus toStatus,
        String note,
        Instant occurredAt
) {
    public static HandoffEventResponse from(HandoffEvent event) {
        return new HandoffEventResponse(
                event.getId(),
                event.getEventType(),
                event.getFromStatus(),
                event.getToStatus(),
                event.getNote(),
                event.getOccurredAt()
        );
    }
}

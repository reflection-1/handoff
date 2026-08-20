package ca.sara.handoff.api;

import ca.sara.handoff.domain.Handoff;
import ca.sara.handoff.domain.HandoffStatus;
import ca.sara.handoff.domain.Priority;
import ca.sara.handoff.domain.ShiftType;

import java.time.Instant;

public record HandoffResponse(
        Long id,
        String title,
        String details,
        String area,
        ShiftType shiftType,
        Priority priority,
        String owner,
        HandoffStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static HandoffResponse from(Handoff handoff) {
        return new HandoffResponse(
                handoff.getId(),
                handoff.getTitle(),
                handoff.getDetails(),
                handoff.getArea(),
                handoff.getShiftType(),
                handoff.getPriority(),
                handoff.getOwner(),
                handoff.getStatus(),
                handoff.getCreatedAt(),
                handoff.getUpdatedAt()
        );
    }
}

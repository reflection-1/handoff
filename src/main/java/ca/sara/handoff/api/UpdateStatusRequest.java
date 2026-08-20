package ca.sara.handoff.api;

import ca.sara.handoff.domain.HandoffStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateStatusRequest(
        @NotNull(message = "status is required")
        HandoffStatus status,

        @Size(max = 200, message = "note must be 200 characters or fewer")
        String note
) {
}

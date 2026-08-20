package ca.sara.handoff.api;

import ca.sara.handoff.domain.Priority;
import ca.sara.handoff.domain.ShiftType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateHandoffRequest(
        @NotBlank(message = "title is required")
        @Size(max = 100, message = "title must be 100 characters or fewer")
        String title,

        @NotBlank(message = "details are required")
        @Size(max = 500, message = "details must be 500 characters or fewer")
        String details,

        @NotBlank(message = "area is required")
        @Size(max = 40, message = "area must be 40 characters or fewer")
        String area,

        @NotNull(message = "shift is required")
        ShiftType shiftType,

        @NotNull(message = "priority is required")
        Priority priority,

        @NotBlank(message = "owner is required")
        @Size(max = 60, message = "owner must be 60 characters or fewer")
        String owner
) {
}

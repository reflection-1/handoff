package ca.sara.handoff.api;

import java.util.Map;

public record MetricsResponse(
        long total,
        long newItems,
        long acknowledged,
        long done,
        long highPriorityOpen,
        Map<String, Long> byArea
) {
}

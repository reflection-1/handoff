package ca.sara.handoff.api;

import ca.sara.handoff.service.HandoffService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final HandoffService handoffService;

    public MetricsController(HandoffService handoffService) {
        this.handoffService = handoffService;
    }

    @GetMapping
    public MetricsResponse metrics() {
        return handoffService.metrics();
    }
}

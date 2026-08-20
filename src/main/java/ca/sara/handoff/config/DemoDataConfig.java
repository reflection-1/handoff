package ca.sara.handoff.config;

import ca.sara.handoff.api.CreateHandoffRequest;
import ca.sara.handoff.api.UpdateStatusRequest;
import ca.sara.handoff.domain.HandoffStatus;
import ca.sara.handoff.domain.Priority;
import ca.sara.handoff.domain.ShiftType;
import ca.sara.handoff.repository.HandoffRepository;
import ca.sara.handoff.service.HandoffService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataConfig {

    @Bean
    @ConditionalOnProperty(name = "handoff.seed.enabled", havingValue = "true", matchIfMissing = true)
    ApplicationRunner seedDemoData(HandoffRepository repository, HandoffService service) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            var printer = service.create(new CreateHandoffRequest(
                    "label printer fades halfway through jobs",
                    "the first few labels print normally, then the text becomes too light to scan.",
                    "cash desk",
                    ShiftType.OPENING,
                    Priority.HIGH,
                    "maya"
            ));
            service.updateStatus(printer.id(), new UpdateStatusRequest(
                    HandoffStatus.ACKNOWLEDGED,
                    "opening shift tested a fresh paper roll"
            ));

            var pickup = service.create(new CreateHandoffRequest(
                    "hold order still needs a pickup check",
                    "the customer received a ready email, but the bag is not in the usual pickup area.",
                    "online orders",
                    ShiftType.MID,
                    Priority.HIGH,
                    "sam"
            ));

            var shelf = service.create(new CreateHandoffRequest(
                    "damaged display shelf needs a morning review",
                    "one corner feels loose. the item is blocked off until facilities can check it.",
                    "sales floor",
                    ShiftType.CLOSING,
                    Priority.MEDIUM,
                    "lee"
            ));
            service.updateStatus(shelf.id(), new UpdateStatusRequest(
                    HandoffStatus.ACKNOWLEDGED,
                    "photo and location shared with the opening lead"
            ));
            service.updateStatus(shelf.id(), new UpdateStatusRequest(
                    HandoffStatus.DONE,
                    "facilities tightened the bracket before opening"
            ));

            service.create(new CreateHandoffRequest(
                    "return bin count is missing one item",
                    "the paper log says eight items, but only seven are in the closing bin.",
                    "stockroom",
                    ShiftType.CLOSING,
                    Priority.MEDIUM,
                    "nora"
            ));

            service.create(new CreateHandoffRequest(
                    "new volunteer still needs register access",
                    "their profile exists, but the register role has not been added yet.",
                    "accounts",
                    ShiftType.OPENING,
                    Priority.LOW,
                    "alex"
            ));
        };
    }
}

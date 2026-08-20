package ca.sara.handoff.repository;

import ca.sara.handoff.domain.HandoffEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HandoffEventRepository extends JpaRepository<HandoffEvent, Long> {
    List<HandoffEvent> findByHandoffIdOrderByOccurredAtAsc(Long handoffId);
}

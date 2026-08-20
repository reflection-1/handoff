package ca.sara.handoff.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "handoff_events")
public class HandoffEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long handoffId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private HandoffStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HandoffStatus toStatus;

    @Column(length = 200)
    private String note;

    @Column(nullable = false)
    private Instant occurredAt;

    protected HandoffEvent() {
    }

    private HandoffEvent(Long handoffId, EventType eventType, HandoffStatus fromStatus,
                         HandoffStatus toStatus, String note) {
        this.handoffId = handoffId;
        this.eventType = eventType;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.note = note;
        this.occurredAt = Instant.now();
    }

    public static HandoffEvent created(Long handoffId) {
        return new HandoffEvent(handoffId, EventType.CREATED, null, HandoffStatus.NEW, "handoff created");
    }

    public static HandoffEvent statusChanged(Long handoffId, HandoffStatus fromStatus,
                                              HandoffStatus toStatus, String note) {
        return new HandoffEvent(handoffId, EventType.STATUS_CHANGED, fromStatus, toStatus, note);
    }

    public Long getId() { return id; }
    public Long getHandoffId() { return handoffId; }
    public EventType getEventType() { return eventType; }
    public HandoffStatus getFromStatus() { return fromStatus; }
    public HandoffStatus getToStatus() { return toStatus; }
    public String getNote() { return note; }
    public Instant getOccurredAt() { return occurredAt; }
}

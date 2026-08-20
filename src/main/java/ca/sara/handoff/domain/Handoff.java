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
@Table(name = "handoffs")
public class Handoff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String details;

    @Column(nullable = false, length = 40)
    private String area;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShiftType shiftType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @Column(nullable = false, length = 60)
    private String owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HandoffStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Handoff() {
    }

    public Handoff(String title, String details, String area, ShiftType shiftType, Priority priority, String owner) {
        Instant now = Instant.now();
        this.title = title;
        this.details = details;
        this.area = area;
        this.shiftType = shiftType;
        this.priority = priority;
        this.owner = owner;
        this.status = HandoffStatus.NEW;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void moveTo(HandoffStatus nextStatus) {
        if (nextStatus == status) {
            return;
        }
        boolean validTransition =
                (status == HandoffStatus.NEW && (nextStatus == HandoffStatus.ACKNOWLEDGED || nextStatus == HandoffStatus.DONE))
                || (status == HandoffStatus.ACKNOWLEDGED && nextStatus == HandoffStatus.DONE);
        if (!validTransition) {
            throw new IllegalStateException("cannot move a handoff from " + status + " to " + nextStatus);
        }
        status = nextStatus;
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDetails() { return details; }
    public String getArea() { return area; }
    public ShiftType getShiftType() { return shiftType; }
    public Priority getPriority() { return priority; }
    public String getOwner() { return owner; }
    public HandoffStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

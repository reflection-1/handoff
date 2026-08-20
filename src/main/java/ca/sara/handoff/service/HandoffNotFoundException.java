package ca.sara.handoff.service;

public class HandoffNotFoundException extends RuntimeException {
    public HandoffNotFoundException(Long id) {
        super("handoff " + id + " was not found");
    }
}

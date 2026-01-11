package io.github.safeslope.antiabuse.service;

import java.time.Instant;

public class CardBlockedException extends RuntimeException {

    public CardBlockedException(Integer skiTicketId, Instant blockedUntil) {
        super("SkiTicket " + skiTicketId + " is blocked until " + blockedUntil);
    }

    public CardBlockedException(Integer skiTicketId) {
        super("SkiTicket " + skiTicketId + " is blocked.");
    }
}

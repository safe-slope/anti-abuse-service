package io.github.safeslope.antiabuse.service;

import java.time.Instant;

public class CardBlockedException extends RuntimeException {

    public CardBlockedException(String cardUid, Instant blockedUntil) {
        super("Card " + cardUid + " is blocked until " + blockedUntil);
    }

    public CardBlockedException(String cardUid) {
        super("Card " + cardUid + " is blocked.");
    }
}

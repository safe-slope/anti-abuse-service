package io.github.safeslope.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "blocked_cards")
public class BlockedCard {

    @Id
    private String cardUid;

    private Instant blockedUntil;
    private String reason;

    // getters/setters
}

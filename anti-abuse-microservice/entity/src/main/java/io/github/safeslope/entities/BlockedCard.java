package io.github.safeslope.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "blocked_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockedCard {

    @Id
    private Integer skiTicketId;

    private Instant blockedUntil;
    private String reason;
}

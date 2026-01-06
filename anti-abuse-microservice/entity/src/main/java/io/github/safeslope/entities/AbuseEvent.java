package io.github.safeslope.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "abuse_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbuseEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardUid;
    private String lockId;
    private String resortId;
    private String action;
    private Instant timestamp;
    private int score;
    private String reason;
}

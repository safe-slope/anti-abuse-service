package io.github.safeslope.antiabuse.model;

import java.time.Instant;

public record DecisionResult(
        String decision,
        String cardState,
        int score,
        String reason,
        Instant blockUntil
) {}

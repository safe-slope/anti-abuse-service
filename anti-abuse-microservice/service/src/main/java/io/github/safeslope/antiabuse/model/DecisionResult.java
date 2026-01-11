package io.github.safeslope.antiabuse.model;

import java.time.Instant;

public record DecisionResult(
        Decision decision,
        CardState cardState,
        String reason,
        Instant blockUntil
) {}

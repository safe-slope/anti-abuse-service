package io.github.safeslope.api.v1.dto;

import io.github.safeslope.antiabuse.model.CardState;
import io.github.safeslope.antiabuse.model.Decision;

public record EvaluateResponse(
        Decision decision,
        CardState cardState,
        String reason,
        Long blockedUntil
) {}

package io.github.safeslope.api.v1.dto;

public record EvaluateResponse(
        String decision,
        String cardState,
        int score,
        String reason,
        Long blockUntil
) {}

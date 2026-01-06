package io.github.safeslope.api.v1.dto;

public record EvaluateResponse(
        String decision,    
        String cardState,   
        String reason,
        Long blockedUntil   
) {}

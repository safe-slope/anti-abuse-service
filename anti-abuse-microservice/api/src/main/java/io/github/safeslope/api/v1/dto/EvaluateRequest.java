package io.github.safeslope.api.v1.dto;


public record EvaluateRequest(
        String cardUid,
         String lockId,
        String lockerId,
        String resortId,
        String action,
        Long timestamp
    
) {}

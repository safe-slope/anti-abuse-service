package io.github.safeslope.antiabuse.model;

public record EvaluateCommand(
        String cardUid,
        String lockId,
        String resortId,
        String action,
        long timestamp
) {}

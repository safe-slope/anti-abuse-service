package io.github.safeslope.antiabuse.model;

public record EvaluateCommand(
        Integer skiTicketId,
        Integer lockId,
        Integer lockerId,
        Integer resortId,
        Action action,
        long timestamp
) {}

package io.github.safeslope.api.v1.dto;

import io.github.safeslope.antiabuse.model.Action;

public record EvaluateRequest(
        Integer skiTicketId,
        Integer lockId,
        Integer lockerId,
        Integer resortId,
        Action action,
        Long timestamp
) {}

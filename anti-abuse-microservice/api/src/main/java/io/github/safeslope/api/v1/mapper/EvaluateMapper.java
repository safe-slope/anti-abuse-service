package io.github.safeslope.api.v1.mapper;

import io.github.safeslope.api.v1.dto.EvaluateRequest;
import io.github.safeslope.api.v1.dto.EvaluateResponse;
import io.github.safeslope.antiabuse.model.DecisionResult;
import io.github.safeslope.antiabuse.model.EvaluateCommand;

import org.springframework.stereotype.Component;

@Component
public class EvaluateMapper {

    public EvaluateCommand toCommand(EvaluateRequest req) {
        long ts = req.timestamp() != null ? req.timestamp() : System.currentTimeMillis();

        return new EvaluateCommand(
                req.cardUid(),
                req.lockId(),
                req.resortId(),
                req.action(),
                ts
        );
    }

    public EvaluateResponse toResponse(DecisionResult result) {
        Long blockedUntil = result.blockUntil() != null
                ? result.blockUntil().toEpochMilli()
                : null;

        return new EvaluateResponse(
                result.decision(),
                result.cardState(),
                result.reason(),
                blockedUntil
        );
    }
}


package io.github.safeslope.api.v1.mapper;

import io.github.safeslope.api.v1.dto.EvaluateRequest;
import io.github.safeslope.api.v1.dto.EvaluateResponse;
import io.github.safeslope.antiabuse.service.model.DecisionResult;
import io.github.safeslope.antiabuse.service.model.EvaluateCommand;
import org.springframework.stereotype.Component;

@Component
public class EvaluateMapper {

    public EvaluateCommand toCommand(EvaluateRequest req) {
        return new EvaluateCommand(
                req.cardUid(),
                req.lockId(),
                req.lockerId(),
                req.resortId(),
                req.action(),
                req.timestamp()
        );
    }

    public EvaluateResponse toResponse(DecisionResult result) {
        return new EvaluateResponse(
                result.decision(),
                result.cardState(),
                result.score(),
                result.reason(),
                result.blockUntil()
        );
    }
}

package io.github.safeslope.api.v1.controller;

import io.github.safeslope.api.v1.dto.EvaluateRequest;
import io.github.safeslope.api.v1.dto.EvaluateResponse;
import io.github.safeslope.api.v1.mapper.EvaluateMapper;
import io.github.safeslope.antiabuse.service.AntiAbuseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/anti-abuse")
public class AntiAbuseController {

    private final AntiAbuseService antiAbuseService;
    private final EvaluateMapper evaluateMapper;

    public AntiAbuseController(AntiAbuseService antiAbuseService, EvaluateMapper evaluateMapper) {
        this.antiAbuseService = antiAbuseService;
        this.evaluateMapper = evaluateMapper;
    }

    @PostMapping("/abuse-verify")
    public EvaluateResponse abuseVerify(@Valid @RequestBody EvaluateRequest req) {
        var cmd = evaluateMapper.toCommand(req);
        var result = antiAbuseService.evaluate(cmd);
        return evaluateMapper.toResponse(result);
    }
}

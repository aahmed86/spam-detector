package com.detector.controller;

import com.detector.model.EmailInput;
import com.detector.model.ScoredEmail;
import com.detector.model.ScoredEmailDto;
import com.detector.service.SpamScoringService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for the spam detector.
 * <h6>Endpoints</h6>
 * <pre>
 * POST   /api/spam/check   Check a single email
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/spam")
public class SpamController {

    private final SpamScoringService spamService;

    public SpamController(SpamScoringService spamService) {
        this.spamService     = spamService;
    }

    /**
     * Check a single email.
     * <pre>
     * POST /api/spam/check
     * { "sender": "attacker@evil.com", "body": "Click here to verify now!" }
     * </pre>
     */
    @PostMapping("/check")
    public ResponseEntity<ScoredEmailDto> checkSpam(@Valid @RequestBody EmailInput email) {
        ScoredEmail scored = spamService.analyzeEmailScore(email);
        return ResponseEntity.ok(spamService.scoredEmailtoDto(scored));
    }
}
package com.detector.controller;

import com.detector.model.*;
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
 * POST   /api/spam/analyze   Check batch of emails
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

    /**
     * Analyze a batch of emails for spam using cross-email similarity with word patterns & lists signals.
     * <pre>
     * POST /api/spam/analyze
     * {
     *   "emails": [
     *     {"sender": "scam@bad.io", "body": "Invest 500 earn 5000 bitcoin!"},
     *     { "sender": "alice@example.com", "body": "Verify your account now!" }
     *   ]
     * }
     * </pre>
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyze(@Valid @RequestBody AnalyzeRequest request) {
        List<ScoredEmail> scored = spamService.analyze(request.emails());
        return ResponseEntity.ok(spamService.toAnalyzeResponse(scored));
    }
}
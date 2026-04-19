package com.detector.model;

import java.util.List;

/**
 * Analyzed Email.
 *
 * @param emailInput      The original email submitted.
 * @param spamProbability Spam tendency score, higher means more likely spam (more than 0.60 considered spam).
 * @param flags           Human-readable reasons that contributed to the score.
 */
public record ScoredEmail(
        EmailInput emailInput,
        double spamProbability,
        List<String> flags
) {}
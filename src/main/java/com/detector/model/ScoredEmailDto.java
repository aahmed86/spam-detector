package com.detector.model;

import java.util.List;

/**
 * Analyzed Email DTO.
 */
public record ScoredEmailDto(
        String sender,
        String bodyPreview,
        double spamProbability,
        String verdict,
        List<String> flags
) {}
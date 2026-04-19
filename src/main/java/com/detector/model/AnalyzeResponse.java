package com.detector.model;

import java.util.List;

/**
 * Analyzed emails.
 */
public record AnalyzeResponse(
        int total,
        int spamCount,
        int suspiciousCount,
        int cleanCount,
        List<ScoredEmailDto> results
) {}

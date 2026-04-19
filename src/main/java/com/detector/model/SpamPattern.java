package com.detector.model;

/**
 * A named pattern used to flag emails.
 *
 * @param name         Short descriptive label shown in diagnostic flags, e.g. "phishing-link".
 * @param patternRegex Java regex evaluated case-insensitively against the email body.
 * @param scoreBoost   Additional probability boost (0.0–1.0) added when this pattern matches.
 */
public record SpamPattern(
        String name,
        String patternRegex,
        double scoreBoost
) {}
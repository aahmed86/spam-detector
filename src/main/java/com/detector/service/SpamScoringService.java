package com.detector.service;

import com.detector.config.ListsConfig;
import com.detector.config.ThresholdConfig;
import com.detector.model.EmailInput;
import com.detector.model.ScoredEmail;
import com.detector.model.ScoredEmailDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.round;

/**
 * Core spam scoring service.
 */
@Service
public class SpamScoringService {

    private static final int PREVIEW_LENGTH = 100;
    private static final String SPAM_LABEL = "SPAM";
    private static final String SUSPICIOUS_LABEL = "SUSPICIOUS";

    private final ListsConfig listsConfig;
    private final ThresholdConfig thresholds;
    private final PatternRegistry patternRegistry;

    public SpamScoringService(ListsConfig listsConfig, ThresholdConfig thresholds, PatternRegistry patternRegistry) {
        this.listsConfig = listsConfig;
        this.thresholds = thresholds;
        this.patternRegistry = patternRegistry;
    }

    public String getVerdictLabel(double probability) {
        if (probability >= thresholds.getSpam()) return SPAM_LABEL;
        if (probability >= thresholds.getSuspicious()) return SUSPICIOUS_LABEL;
        return "CLEAN";
    }

    public ScoredEmailDto scoredEmailtoDto(ScoredEmail se) {
        String body = se.emailInput().body();
        String preview = body.length() <= PREVIEW_LENGTH ? body : body.substring(0, PREVIEW_LENGTH) + "…";
        String verdict = getVerdictLabel(se.spamProbability());
        return new ScoredEmailDto(
                se.emailInput().sender(),
                preview,
                se.spamProbability(),
                verdict,
                se.flags()
        );
    }

    public ScoredEmail analyzeEmailScore(EmailInput email) {
        List<String> flags = new ArrayList<>();

        String domain = extractDomain(email.sender());
        if (isWhitelisted(domain)) {
            return new ScoredEmail(email, 0.0, List.of("domain-whitelisted"));
        }

        if (isBlacklisted(domain)) {
            return new ScoredEmail(email, thresholds.getBlacklistDomainScore(), List.of("domain-blacklisted: " + domain));
        }

        List<String> matchedPatterns = patternRegistry.matchingPatterns(email.body());
        double patternBoost = patternRegistry.totalBoost(email.body());
        matchedPatterns.forEach(p -> flags.add("pattern: " + p));

        double keywordBoost = 0.0;
        String lowerBody = email.body().toLowerCase();
        for (String kw : listsConfig.getBlacklistWords()) {
            if (lowerBody.contains(kw.toLowerCase())) {
                keywordBoost = Math.min(keywordBoost + thresholds.getBlacklistWordBoost(), 1.0);
                flags.add("keyword: " + kw);
            }
        }

        double rawScore = thresholds.getWordpatternsweight() * patternBoost + thresholds.getWordlistsweight() * keywordBoost;

        return new ScoredEmail(email, round(rawScore), flags);
    }

    private String extractDomain(String sender) {
        int at = sender != null ? sender.indexOf('@') : -1;
        return at >= 0 ? sender.substring(at + 1).toLowerCase() : (sender != null ? sender.toLowerCase() : "");
    }

    private boolean isWhitelisted(String domain) {
        return listsConfig.getWhitelistDomains().stream()
                .anyMatch(d -> d.equalsIgnoreCase(domain));
    }

    private boolean isBlacklisted(String domain) {
        return listsConfig.getBlacklistDomains().stream()
                .anyMatch(d -> d.equalsIgnoreCase(domain));
    }

}
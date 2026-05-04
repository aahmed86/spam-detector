package com.detector.service;

import com.detector.config.ListsConfig;
import com.detector.config.ThresholdConfig;
import com.detector.model.AnalyzeResponse;
import com.detector.model.EmailInput;
import com.detector.model.ScoredEmail;
import com.detector.model.ScoredEmailDto;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core spam scoring service.
 */
@Service
public class SpamScoringService {

    private static final int PREVIEW_LENGTH = 100;
    private static final String SPAM_LABEL = "SPAM";
    private static final String SUSPICIOUS_LABEL = "SUSPICIOUS";
    private static final String CLEAN_LABEL = "CLEAN";

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "is", "at", "which", "on", "and", "a", "an", "to", "of",
            "for", "in", "your", "you", "our", "we", "i", "it", "its",
            "this", "that", "with", "be", "are", "was", "have", "has",
            "will", "from", "by", "as", "or", "not", "but", "if", "so"
    );

    private final ListsConfig listsConfig;
    private final ThresholdConfig thresholds;
    private final PatternRegistry patternRegistry;

    public SpamScoringService(ListsConfig listsConfig, ThresholdConfig thresholds, PatternRegistry patternRegistry) {
        this.listsConfig = listsConfig;
        this.thresholds = thresholds;
        this.patternRegistry = patternRegistry;
    }

    public ScoredEmail analyzeEmailScore(EmailInput email) {
        String domain = extractDomain(email.sender());
        if (isWhitelisted(domain)) {
            return new ScoredEmail(email, 0.0, List.of("domain-whitelisted"));
        }

        if (isBlacklisted(domain)) {
            return new ScoredEmail(email, thresholds.getBlacklistDomainScore(), List.of("domain-blacklisted: " + domain));
        }

        TextSignals signals = extractSignals(email.body());

        List<String> flags = new ArrayList<>(signals.flags());

        double rawScore = thresholds.getSingleMailWordPatterns() * signals.patternBoost() + thresholds.getSingleMailWordLists() * signals.keywordBoost();
        return new ScoredEmail(email, saturate(rawScore), flags);
    }

    /**
     * Analyse a batch of emails and return a {@link ScoredEmail} for each one.
     *
     * @param emails a list of {@code EmailInput}.
     * @return list in the same order as the input.
     */
    public List<ScoredEmail> analyze(List<EmailInput> emails) {
        if (emails == null || emails.isEmpty()) return List.of();
        if (emails.size() == 1) {
            return List.of(analyzeEmailScore(emails.getFirst()));
        }

        List<Set<String>> shingleSets = emails.stream()
                .map(e -> createNormalizedShingles(e.body()))
                .toList();

        Map<String, Long> senderCounts = emails.stream()
                .collect(Collectors.groupingBy(e -> extractDomain(e.sender()), Collectors.counting()));

        boolean smallBatch = emails.size() < 5;

        List<ScoredEmail> results = new ArrayList<>();
        for (int i = 0; i < emails.size(); i++) {
            EmailInput email = emails.get(i);

            String domain = extractDomain(email.sender());
            if (isWhitelisted(domain)) {
                results.add(new ScoredEmail(email, 0.0, List.of("domain-whitelisted")));
                continue;
            }

            if (isBlacklisted(domain)) {
                results.add(new ScoredEmail(email, thresholds.getBlacklistDomainScore(), List.of("domain-blacklisted: " + domain)));
                continue;
            }

            TextSignals signals = extractSignals(email.body());

            List<String> flags = new ArrayList<>(signals.flags());

            double maxSimilarity = 0.0;
            int similarCount = 0;

            for (int j = 0; j < emails.size(); j++) {
                if (i == j) continue;
                double sim = jaccardSimilarity(shingleSets.get(i), shingleSets.get(j));
                if (sim > maxSimilarity) maxSimilarity = sim;
                if (sim >= thresholds.getSimilarity()) similarCount++;
            }

            double clusterDensity = (double) similarCount / (emails.size() - 1);
            if (smallBatch) clusterDensity *= 0.5;

            if (maxSimilarity >= thresholds.getSimilarity()) {
                flags.add(String.format("similar-to-%d-other(s) (max-jaccard=%.2f)", similarCount, maxSimilarity));
            }

            double floodBoost = 0.0;
            long senderCount = senderCounts.getOrDefault(domain, 0L);
            if (senderCount >= thresholds.getSenderFlood()) {
                floodBoost = thresholds.getSenderFloodBoost();
                flags.add("sender-flood:" + domain + " (" + senderCount + " emails)");
            }

            double rawScore = thresholds.getWordSimilarity() * maxSimilarity + thresholds.getWordDensity() * clusterDensity
                    + thresholds.getWordPatterns() * signals.patternBoost() + thresholds.getWordLists() * signals.keywordBoost() + floodBoost;
            results.add(new ScoredEmail(email, saturate(rawScore), flags));
        }
        return results;
    }

    private String extractDomain(String sender) {
        if (sender == null) {
            return "";
        }

        int at = sender.indexOf('@');
        return (at >= 0 ? sender.substring(at + 1) : sender).toLowerCase();
    }

    private boolean isWhitelisted(String domain) {
        return listsConfig.getWhitelistDomains().stream()
                .anyMatch(d -> d.equalsIgnoreCase(domain));
    }

    private boolean isBlacklisted(String domain) {
        return listsConfig.getBlacklistDomains().stream()
                .anyMatch(d -> d.equalsIgnoreCase(domain));
    }

    private String getVerdictLabel(double probability) {
        if (probability >= thresholds.getSpam()) return SPAM_LABEL;
        if (probability >= thresholds.getSuspicious()) return SUSPICIOUS_LABEL;
        return CLEAN_LABEL;
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

    public AnalyzeResponse toAnalyzeResponse(List<ScoredEmail> scored) {
        List<ScoredEmailDto> dtos = scored.stream().map(this::scoredEmailtoDto).toList();
        int spam = (int) dtos.stream().filter(d -> SPAM_LABEL.equals(d.verdict())).count();
        int susp = (int) dtos.stream().filter(d -> SUSPICIOUS_LABEL.equals(d.verdict())).count();
        int clean = (int) dtos.stream().filter(d -> CLEAN_LABEL.equals(d.verdict())).count();
        return new AnalyzeResponse(dtos.size(), spam, susp, clean, dtos);
    }

    /**
     * Builds a set of word-level n-grams (shingles) from the email body.
     */
    public Set<String> createNormalizedShingles(String text) {
        List<String> words = Arrays.stream(text.toLowerCase().replaceAll("[^a-z0-9 ]", " ").split("\\s+"))
                .filter(w -> !STOP_WORDS.contains(w)).filter(w -> !w.isBlank()).toList();

        Set<String> shingles = new HashSet<>();

        if (words.size() < thresholds.getShingleSize()) {
            shingles.addAll(words);
            return shingles;
        }

        for (int i = 0; i <= words.size() - thresholds.getShingleSize(); i++) {
            shingles.add(words.get(i) + " " + words.get(i + 1));
        }
        return shingles;
    }

    /**
     * Jaccard similarity.
     * Returns 0.0 if either set is empty.
     */
    public double jaccardSimilarity(Set<String> s1, Set<String> s2) {
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(s1);
        intersection.retainAll(s2);
        Set<String> union = new HashSet<>(s1);
        union.addAll(s2);
        return (double) intersection.size() / union.size();
    }

    private double saturate(double raw) {
        return 1.0 - Math.exp(-2.5 * raw);
    }

    private record TextSignals(double patternBoost, double keywordBoost, List<String> flags) {}

    private TextSignals extractSignals(String body) {
        List<String> flags = new ArrayList<>();

        List<String> matchedPatterns = patternRegistry.matchingPatterns(body);
        double patternBoost = patternRegistry.totalBoost(body);
        matchedPatterns.forEach(p -> flags.add("pattern: " + p));

        double keywordBoost = 0.0;
        String lowerBody = body.toLowerCase();

        for (String kw : listsConfig.getBlacklistWords()) {
            if (lowerBody.contains(kw.toLowerCase())) {
                keywordBoost = Math.min(keywordBoost + thresholds.getBlacklistWordBoost(), 1.0);
                flags.add("keyword: " + kw);
            }
        }

        return new TextSignals(patternBoost, keywordBoost, flags);
    }
}
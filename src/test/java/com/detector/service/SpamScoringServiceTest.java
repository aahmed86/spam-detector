package com.detector.service;

import com.detector.config.ListsConfig;
import com.detector.config.ThresholdConfig;
import com.detector.model.EmailInput;
import com.detector.model.ScoredEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SpamScoringServiceTest {

    private SpamScoringService service;

    @BeforeEach
    void setUp() {
        ListsConfig lists = new ListsConfig();
        lists.setWhitelistDomains(List.of());
        lists.setBlacklistDomains(List.of());
        lists.setBlacklistWords(List.of());

        ThresholdConfig thresholds = new ThresholdConfig();
        thresholds.setWordSimilarity(0.50);
        thresholds.setWordDensity(0.10);
        thresholds.setSenderFloodBoost(0.0);

        service = new SpamScoringService(lists, thresholds, new PatternRegistry());
    }

    // analyze()
    @Test
    void identicalEmailsGiveSameScore() {
        EmailInput e1 = new EmailInput("a@test.com", "Quarterly report discussion scheduled.");
        EmailInput e2 = new EmailInput("b@test.com", "Quarterly report discussion scheduled.");

        List<ScoredEmail> results = service.analyze(List.of(e1, e2));

        assertThat(results).hasSize(2);

        double score1 = results.get(0).spamProbability();
        double score2 = results.get(1).spamProbability();
        
        assertThat(score1).isEqualTo(score2).isBetween(0.0, 1.0);
    }

    @Test
    void sameBatchSameContentStaysConsistent() {
        List<EmailInput> emails = List.of(
                new EmailInput("a@test.com", "Quarterly report discussion scheduled."),
                new EmailInput("b@test.com", "Quarterly report discussion scheduled."),
                new EmailInput("c@test.com", "Quarterly report discussion scheduled."),
                new EmailInput("d@test.com", "Quarterly report discussion scheduled.")
        );

        List<ScoredEmail> results = service.analyze(emails);

        double firstScore = results.getFirst().spamProbability();

        results.forEach(r -> assertThat(r.spamProbability()).isEqualTo(firstScore));
        assertThat(firstScore).isGreaterThan(0.0).isLessThan(1.0);
    }

    // jaccardSimilarity()
    @Test
    void identicalTextHasPerfectSimilarity() {
        Set<String> shingles = service.createNormalizedShingles("hello world example");

        assertThat(service.jaccardSimilarity(shingles, shingles)).isEqualTo(1.0);
    }

    @Test
    void differentTextsHaveLowSimilarity() {
        Set<String> a = service.createNormalizedShingles("hello world");
        Set<String> b = service.createNormalizedShingles("bitcoin investment profit");

        double similarity = service.jaccardSimilarity(a, b);

        assertThat(similarity).isLessThan(0.3);
    }

    @Test
    void similarityIsSymmetric() {
        Set<String> a = service.createNormalizedShingles("Claim your prize now!");
        Set<String> b = service.createNormalizedShingles("Claim your reward now!");

        assertThat(service.jaccardSimilarity(a, b)).isEqualTo(service.jaccardSimilarity(b, a));
    }
}

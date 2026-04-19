package com.detector.service;

import com.detector.model.SpamPattern;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * In-memory registry of compiled {@link SpamPattern}s.
 */
@Component
public class PatternRegistry {

    private record CompiledPattern(SpamPattern meta, Pattern compiled) {}

    private final List<CompiledPattern> patterns = new CopyOnWriteArrayList<>();

    public PatternRegistry() {
        seedDefaults();
    }

    /**
     * Register a new pattern.
     * Duplicate names are silently replaced.
     */
    public synchronized void add(SpamPattern p) {
        // Remove any existing pattern with the same name first
        patterns.removeIf(cp -> cp.meta().name().equalsIgnoreCase(p.name()));
        patterns.add(new CompiledPattern(p, Pattern.compile(p.patternRegex(), Pattern.CASE_INSENSITIVE)));
    }

    /**
     * Remove a pattern by name.
     * Returns {@code true} if it was present.
     */
    public synchronized boolean remove(String name) {
        return patterns.removeIf(cp -> cp.meta().name().equalsIgnoreCase(name));
    }

    /**
     * Returns an unmodifiable snapshot of all registered pattern metadata.
     */
    public List<SpamPattern> list() {
        return patterns.stream().map(CompiledPattern::meta).toList();
    }

    /**
     * Evaluates all patterns against {@code body}.
     * @return list of pattern names that matched.
     */
    public List<String> matchingPatterns(String body) {
        return patterns.stream()
                .filter(cp -> cp.compiled().matcher(body).find())
                .map(cp -> cp.meta().name())
                .toList();
    }

    /**
     * Total score boost from all matching patterns (capped at 1.0).
     */
    public double totalBoost(String body) {
        double boost = patterns.stream()
                .filter(cp -> cp.compiled().matcher(body).find())
                .mapToDouble(cp -> cp.meta().scoreBoost())
                .sum();
        return Math.min(boost, 1.0);
    }

    private void seedDefaults() {
        List<SpamPattern> defaults = List.of(
                new SpamPattern("phishing-verify-account",
                        "verify\\s+(your\\s+)?(account|identity|email|mailbox|details)",
                        0.25),
                new SpamPattern("phishing-account-suspended",
                        "(account|mailbox)\\s+(is\\s+)?(suspended|restricted|limited|closed|deactivated|terminated)",
                        0.25),
                new SpamPattern("phishing-urgent-action",
                        "(urgent|immediate(ly)?|action required|act now|right away|asap).*" +
                                "(click|verify|confirm|update|validate|login|sign in)",
                        0.30),
                new SpamPattern("phishing-click-link",
                        "click (here|this link|the link|below)",
                        0.15),
                new SpamPattern("prize-winner",
                        "(you (have |'ve )?(won|been selected|are our lucky winner)|congratulations.{0,30}prize)",
                        0.30),
                new SpamPattern("prize-claim",
                        "claim (your |the )?(prize|reward|gift|iPhone|laptop|car)",
                        0.25),
                new SpamPattern("prize-limited-time",
                        "(limited (time|offer)|expires? (soon|in \\d+ hours?|today)|before it.{0,10}(late|expires?))",
                        0.15),
                new SpamPattern("financial-get-rich",
                        "(earn|make|invest).{0,30}\\$\\d+(,\\d{3})?(\\.\\d+)?.{0,30}(days?|week|month|hour)",
                        0.35),
                new SpamPattern("financial-guaranteed-returns",
                        "(guaranteed|100%.{0,10}(profit|return|safe)|risk.?free).{0,40}invest",
                        0.35),
                new SpamPattern("financial-crypto-trading",
                        "(bitcoin|crypto|btc).{0,40}(trading|investment|platform|algorithm|bot)",
                        0.20),
                new SpamPattern("spam-unsubscribe-trick",
                        "to (stop receiving|unsubscribe|opt.?out).{0,60}(click|visit|go to)",
                        0.10),
                new SpamPattern("spam-all-caps-urgency",
                        "\\b(URGENT|WARNING|ALERT|NOTICE|IMPORTANT|FINAL NOTICE)\\b",
                        0.10)
        );

        defaults.forEach(this::add);
    }
}
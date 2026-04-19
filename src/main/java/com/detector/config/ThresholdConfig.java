package com.detector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Spam and suspicious thresholds.
 */
@Configuration
@ConfigurationProperties(prefix = "spam-detector.thresholds")
public class ThresholdConfig {
    private double spam = 0.60;
    private double suspicious = 0.30;
    private double blacklistDomainScore = 1.0;
    private double blacklistWordBoost = 0.10;
    private double wordlistsweight = 0.10;
    private double wordpatternsweight = 0.10;

    public double getSpam() { return spam; }
    public void setSpam(double spam) { this.spam = spam; }

    public double getSuspicious() { return suspicious; }
    public void setSuspicious(double suspicious) { this.suspicious = suspicious; }

    public double getBlacklistDomainScore() { return blacklistDomainScore; }
    public void setBlacklistDomainScore(double v) { this.blacklistDomainScore = v; }

    public double getBlacklistWordBoost() { return blacklistWordBoost; }
    public void setBlacklistWordBoost(double v) { this.blacklistWordBoost = v; }

    public double getWordlistsweight() { return wordlistsweight; }
    public void setWordlistsweight(double wordlistsweight) { this.wordlistsweight = wordlistsweight; }

    public double getWordpatternsweight() { return wordpatternsweight; }
    public void setWordpatternsweight(double wordpatternsweight) { this.wordpatternsweight = wordpatternsweight; }
}
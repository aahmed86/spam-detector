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
    private int shingleSize = 2;
    private double similarity = 0.20;
    private double wordSimilarity = 0.70;
    private double wordDensity = 0.10;
    private double wordLists = 0.10;
    private double wordPatterns = 0.10;
    private int senderFlood = 3;
    private double senderFloodBoost = 0.15;
    private double singleMailWordLists = 0.50;
    private double singleMailWordPatterns = 0.50;

    public double getSpam() { return spam; }
    public void setSpam(double spam) { this.spam = spam; }

    public double getSuspicious() { return suspicious; }
    public void setSuspicious(double suspicious) { this.suspicious = suspicious; }

    public double getBlacklistDomainScore() { return blacklistDomainScore; }
    public void setBlacklistDomainScore(double v) { this.blacklistDomainScore = v; }

    public double getBlacklistWordBoost() { return blacklistWordBoost; }
    public void setBlacklistWordBoost(double v) { this.blacklistWordBoost = v; }

    public int getShingleSize() { return shingleSize; }
    public void setShingleSize(int shingleSize) { this.shingleSize = shingleSize; }

    public double getSimilarity() { return similarity; }
    public void setSimilarity(double similarity) { this.similarity = similarity; }

    public double getWordSimilarity() { return wordSimilarity; }
    public void setWordSimilarity(double wordSimilarity) { this.wordSimilarity = wordSimilarity; }

    public double getWordDensity() { return wordDensity; }
    public void setWordDensity(double wordDensity) { this.wordDensity = wordDensity; }

    public double getWordLists() { return wordLists; }
    public void setWordLists(double wordLists) { this.wordLists = wordLists; }

    public double getWordPatterns() { return wordPatterns; }
    public void setWordPatterns(double wordPatterns) { this.wordPatterns = wordPatterns; }

    public int getSenderFlood() { return senderFlood; }
    public void setSenderFlood(int senderFlood) { this.senderFlood = senderFlood; }

    public double getSenderFloodBoost() { return senderFloodBoost; }
    public void setSenderFloodBoost(double senderFloodBoost) { this.senderFloodBoost = senderFloodBoost; }

    public double getSingleMailWordLists() { return singleMailWordLists; }
    public void setSingleMailWordLists(double singleMailWordLists) { this.singleMailWordLists = singleMailWordLists; }

    public double getSingleMailWordPatterns() { return singleMailWordPatterns; }
    public void setSingleMailWordPatterns(double singleMailWordPatterns) { this.singleMailWordPatterns = singleMailWordPatterns; }
}
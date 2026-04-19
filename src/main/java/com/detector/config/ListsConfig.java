package com.detector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Domain and keyword lists.
 */
@Configuration
@ConfigurationProperties(prefix = "spam-detector")
public class ListsConfig {

    private List<String> whitelistDomains = List.of();
    private List<String> blacklistDomains = List.of();
    private List<String> blacklistWords = List.of();

    public List<String> getWhitelistDomains() { return whitelistDomains; }
    public void setWhitelistDomains(List<String> v) { this.whitelistDomains = v; }

    public List<String> getBlacklistDomains() { return blacklistDomains; }
    public void setBlacklistDomains(List<String> v) { this.blacklistDomains = v; }

    public List<String> getBlacklistWords() { return blacklistWords; }
    public void setBlacklistWords(List<String> v) { this.blacklistWords = v; }
}
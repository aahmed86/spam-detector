# spam-detector
A Java/Spring Boot app to identify phishing and scam campaigns.

## Scoring mechanism
### 1. Whitelist domain check
Check if the sender's domain is whitelisted the score is forced to 0.0 and further checks are skipped.
### 2. Blacklist domain check
Check if the sender's domain is blacklisted the score is floored at **blacklistDomainScore** (default 1.0) and further checks are skipped.
### 3. Pattern matching check
Check against known regex patterns (phishing, pharma, prize, …) each add a **scoreBoost** to the running total.</li>
### 4. Blacklisted keyword check
Check against configurable keyword list adds per-word boosts.

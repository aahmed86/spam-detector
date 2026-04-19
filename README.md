# spam-detector
A Java/Spring Boot app to identify phishing and scam campaigns.

---

## Scoring mechanism
**1. Whitelist domain check**
Check if the sender's domain is whitelisted the score is forced to 0.0 and further checks are skipped.

**2. Blacklist domain check**
Check if the sender's domain is blacklisted the score is floored at **blacklistDomainScore** (default 1.0) and further checks are skipped.

**3. Pattern matching check**
Check against known regex patterns (phishing, pharma, prize, …) each add a **scoreBoost** to the running total.</li>

**4. Blacklisted keyword check**
Check against configurable keyword list adds per-word boosts.

**5. Similarity check**
Check Jaccard similarity over word-shingles across the whole batch. Two flavours are combined:
**Max similarity** highest Jaccard against any other email.
**Cluster density** share of other emails that are "similar" (above a threshold).
This check works only with the batch currently, can be extended later for single emails when results are cached or persisted.

**6. Sender flood signal check**
Check if the same sender address appears many times in the batch, all their emails receive a boost.

---

## Background & Theory

The core of this service's cross-email detection logic is based on **Set Similarity** and **N-Gram Tokenization**.

### 1. Shingling (N-Grams)
To detect "fuzzy" matches where spammers change small details (synonyms, dates, links), we break the email body into overlapping sequences of words called "shingles."

**Concept:** Instead of matching the whole string, we match the underlying structure.

**Reading:** [An Introduction to N-Grams (Stanford NLP)](https://web.stanford.edu/~jurafsky/slp3/3.pdf)

### 2. Jaccard Similarity
Once emails are converted into sets of shingles, we calculate the Jaccard Index to determine how much of the content is shared.

**Reading:** [Jaccard Index](https://en.wikipedia.org/wiki/Jaccard_index), [Mining of Massive Datasets - Chapter 3: Finding Similar Items](http://www.mmds.org/mmds/v2.1/ch03.pdf)

### 3. Saturation
Checks are combined using a weighted approach and normalized via a saturation function to ensure the final score is a probability between `0.0` and `1.0`.

---

## Interactive API Documentation

I have included built-in OpenAPI 3 support via Swagger UI. This allows you to explore the data models, test the scoring logic, and execute API calls directly from your browser without using Terminal/CURL:

[Swagger UI (Interactive)](http://localhost:8080/swagger-ui/index.html)

[OpenAPI JSON Docs](http://localhost:8080/v3/api-docs)

Thresholds are defined in **application.yml** 

---

## What is left? / Next Steps
* **Standardized Unit Testing:** Implement JUnit 5 suites to isolate and verify the core math (Jaccard similarity and saturation curves) in the service layer, while using MockMvc in the controller layer to ensure API contracts and HTTP status codes remain stable.
* **Automated Smoke Suite:** Formalize the existing CommandLineRunner logic into a dedicated Bootstrap Test Suite that runs on application startup. This provides an immediate "Green/Red" health check of our scoring sensitivity across all six core attack scenarios before the service accepts traffic.
* **Dynamic Pattern Management:** Expose a dedicated RESTful Pattern API (GET, POST, DELETE) to register new regex signatures or adjust scoreBoost values at runtime.
* **Persistent Pattern Storage:** Replace the volatile ConcurrentHashMap with a Database using Spring Data JPA. This ensures that any patterns added via API survive service restarts and can be shared across multiple running instances of the detector.

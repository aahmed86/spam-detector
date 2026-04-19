package com.detector.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request body for {@code POST /api/spam/analyze}.
 */
public record AnalyzeRequest(
        @NotEmpty(message = "emails list must not be empty")
        @Valid
        List<EmailInput> emails
) { }
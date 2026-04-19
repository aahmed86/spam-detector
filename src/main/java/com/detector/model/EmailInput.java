package com.detector.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents submitted email for spam analysis.
 *
 * @param sender  The sender's email address.
 * @param body    The content of the email (plain-text body).
 */
public record EmailInput(
        @NotBlank(message = "Sender must not be blank")
        String sender,

        @NotBlank(message = "Body must not be blank")
        String body
) {}
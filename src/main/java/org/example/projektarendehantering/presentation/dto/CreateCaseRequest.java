package org.example.projektarendehantering.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Incoming payload for creating a new case.
 */
public record CreateCaseRequest(
        @NotNull
        @Size(min = 1, max = 200)
        String title,
        @NotNull
        @Size(min = 1, max = 4000)
        String description
) {
}


package org.example.projektarendehantering.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Case identifier (value object).
 */
public record CaseId(UUID value) {

    public CaseId {
        Objects.requireNonNull(value, "value");
    }
}


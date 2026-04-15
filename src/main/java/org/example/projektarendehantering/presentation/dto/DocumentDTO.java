package org.example.projektarendehantering.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentDTO(
    UUID id,
    String fileName,
    String contentType,
    long fileSize,
    Instant uploadedAt,
    UUID uploadedBy,
    UUID caseId
) {}

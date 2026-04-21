package org.example.projektarendehantering.application.service;

import java.time.Instant;
import java.util.UUID;

public record CaseRealtimeEvent(
        String eventType,
        UUID caseId,
        Instant occurredAt,
        String message
) {
}

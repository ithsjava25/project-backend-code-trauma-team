package org.example.projektarendehantering.domain;

import java.util.Objects;

/**
 * Domain event emitted when a case lifecycle milestone happens.
 */
public record CaseEvent(
        AuditEventType type,
        CaseId caseId
) {
    public static CaseEvent caseCreated(CaseId caseId) {
        Objects.requireNonNull(caseId, "caseId");
        return new CaseEvent(AuditEventType.CASE_CREATED, caseId);
    }
}


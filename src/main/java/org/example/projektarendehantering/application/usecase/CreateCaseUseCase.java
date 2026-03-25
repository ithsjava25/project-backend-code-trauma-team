package org.example.projektarendehantering.application.usecase;

import org.example.projektarendehantering.application.ports.AuditLogPort;
import org.example.projektarendehantering.application.ports.CaseEventPublisherPort;
import org.example.projektarendehantering.application.ports.CaseRepositoryPort;
import org.example.projektarendehantering.application.ports.CurrentUserPort;
import org.example.projektarendehantering.domain.AuditEvent;
import org.example.projektarendehantering.domain.Case;
import org.example.projektarendehantering.domain.CaseEvent;
import org.example.projektarendehantering.domain.CaseId;
import org.example.projektarendehantering.domain.CasePermissions;
import org.example.projektarendehantering.common.Actor;
import org.springframework.stereotype.Service;

@Service
public class CreateCaseUseCase {

    private final CurrentUserPort currentUserPort;
    private final CaseRepositoryPort caseRepositoryPort;
    private final AuditLogPort auditLogPort;
    private final CaseEventPublisherPort caseEventPublisherPort;

    public CreateCaseUseCase(
            CurrentUserPort currentUserPort,
            CaseRepositoryPort caseRepositoryPort,
            AuditLogPort auditLogPort,
            CaseEventPublisherPort caseEventPublisherPort
    ) {
        this.currentUserPort = currentUserPort;
        this.caseRepositoryPort = caseRepositoryPort;
        this.auditLogPort = auditLogPort;
        this.caseEventPublisherPort = caseEventPublisherPort;
    }

    public CreateCaseResult execute(CreateCaseCommand command) {
        Actor actor = currentUserPort.currentUser();
        CasePermissions.assertCanCreate(actor);

        Case caseToCreate = Case.create(actor, command.title(), command.description());
        CaseId caseId = caseToCreate.id();
        caseRepositoryPort.save(caseToCreate);

        AuditEvent auditEvent = AuditEvent.caseCreated(actor, caseId, command.title(), command.description());
        auditLogPort.append(auditEvent);

        CaseEvent caseEvent = CaseEvent.caseCreated(caseId);
        caseEventPublisherPort.publishCaseEvent(caseEvent);

        return new CreateCaseResult(caseId);
    }
}


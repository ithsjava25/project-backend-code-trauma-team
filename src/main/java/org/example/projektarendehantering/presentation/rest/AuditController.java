package org.example.projektarendehantering.presentation.rest;

import lombok.RequiredArgsConstructor;
import org.example.projektarendehantering.application.service.AuditService;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.example.projektarendehantering.presentation.dto.AuditEventDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.Role;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;
    private final SecurityActorAdapter securityActorAdapter;

    @GetMapping
    public ResponseEntity<Page<AuditEventDTO>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) UUID caseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 200),
                Sort.by(Sort.Direction.DESC, "occurredAt")
        );

        return ResponseEntity.ok(auditService.listEvents(securityActorAdapter.currentUser(), from, to, caseId, pageable));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        Actor actor = securityActorAdapter.currentUser();
        if (actor.role() != Role.MANAGER) {
            throw new org.example.projektarendehantering.common.NotAuthorizedException("Only managers can view the audit stream");
        }
        return auditService.createEmitter();
    }
}

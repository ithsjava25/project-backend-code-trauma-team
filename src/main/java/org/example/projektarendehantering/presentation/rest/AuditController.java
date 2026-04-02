package org.example.projektarendehantering.presentation.rest;

import org.example.projektarendehantering.application.service.AuditService;
import org.example.projektarendehantering.infrastructure.security.HeaderCurrentUserAdapter;
import org.example.projektarendehantering.presentation.dto.AuditEventDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;
    private final HeaderCurrentUserAdapter currentUserAdapter;

    public AuditController(AuditService auditService, HeaderCurrentUserAdapter currentUserAdapter) {
        this.auditService = auditService;
        this.currentUserAdapter = currentUserAdapter;
    }

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

        return ResponseEntity.ok(auditService.listEvents(currentUserAdapter.currentUser(), from, to, caseId, pageable));
    }
}


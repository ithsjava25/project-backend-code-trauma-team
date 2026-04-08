package org.example.projektarendehantering.audit.presentation;

import org.example.projektarendehantering.audit.application.AuditService;
import org.example.projektarendehantering.audit.application.AuditEventDTO;
import org.example.projektarendehantering.identity.infrastructure.SecurityActorAdapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.UUID;

@Controller
public class AuditUiController {

    private final AuditService auditService;
    private final SecurityActorAdapter securityActorAdapter;

    public AuditUiController(AuditService auditService, SecurityActorAdapter securityActorAdapter) {
        this.auditService = auditService;
        this.securityActorAdapter = securityActorAdapter;
    }

    @GetMapping("/ui/audit")
    public String list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) UUID caseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 200),
                Sort.by(Sort.Direction.DESC, "occurredAt")
        );

        Page<AuditEventDTO> events = auditService.listEvents(securityActorAdapter.currentUser(), from, to, caseId, pageable);
        model.addAttribute("events", events);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("caseId", caseId);
        return "audit/list";
    }
}

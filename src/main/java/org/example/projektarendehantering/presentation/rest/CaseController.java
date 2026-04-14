package org.example.projektarendehantering.presentation.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.projektarendehantering.application.service.CaseService;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.example.projektarendehantering.presentation.dto.CaseAssignmentDTO;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;
    private final SecurityActorAdapter securityActorAdapter;

    @PostMapping
    public ResponseEntity<CaseDTO> createCase(@RequestBody @Valid CaseDTO caseDTO) {
        CaseDTO created = caseService.createCase(securityActorAdapter.currentUser(), caseDTO);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaseDTO> getCase(@PathVariable UUID id) {
        return caseService.getCase(securityActorAdapter.currentUser(), id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CaseDTO>> getAllCases() {
        return ResponseEntity.ok(caseService.getAllCases(securityActorAdapter.currentUser()));
    }

    @PutMapping("/{id}/assignments")
    public ResponseEntity<CaseDTO> assignUsers(@PathVariable UUID id, @RequestBody CaseAssignmentDTO dto) {
        return ResponseEntity.ok(caseService.assignUsers(securityActorAdapter.currentUser(), id, dto));
    }
}

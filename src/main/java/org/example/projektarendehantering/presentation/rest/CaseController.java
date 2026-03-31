package org.example.projektarendehantering.presentation.rest;

import jakarta.validation.Valid;
import org.example.projektarendehantering.application.service.CaseService;
import org.example.projektarendehantering.infrastructure.security.HeaderCurrentUserAdapter;
import org.example.projektarendehantering.presentation.dto.CaseAssignmentDTO;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;
    private final HeaderCurrentUserAdapter currentUserAdapter;

    public CaseController(CaseService caseService, HeaderCurrentUserAdapter currentUserAdapter) {
        this.caseService = caseService;
        this.currentUserAdapter = currentUserAdapter;
    }

    @PostMapping
    public ResponseEntity<CaseDTO> createCase(@RequestBody @Valid CaseDTO caseDTO) {
        CaseDTO created = caseService.createCase(currentUserAdapter.currentUser(), caseDTO);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaseDTO> getCase(@PathVariable UUID id) {
        return caseService.getCase(currentUserAdapter.currentUser(), id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CaseDTO>> getAllCases() {
        return ResponseEntity.ok(caseService.getAllCases(currentUserAdapter.currentUser()));
    }

    @PutMapping("/{id}/assignments")
    public ResponseEntity<CaseDTO> assignUsers(@PathVariable UUID id, @RequestBody CaseAssignmentDTO dto) {
        return ResponseEntity.ok(caseService.assignUsers(currentUserAdapter.currentUser(), id, dto));
    }
}

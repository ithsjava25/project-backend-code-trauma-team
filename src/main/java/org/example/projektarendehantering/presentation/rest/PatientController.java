package org.example.projektarendehantering.presentation.rest;

import jakarta.validation.Valid;
import org.example.projektarendehantering.application.service.CaseService;
import org.example.projektarendehantering.application.service.PatientService;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.example.projektarendehantering.presentation.dto.PatientCreateDTO;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.example.projektarendehantering.presentation.dto.PatientDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final CaseService caseService;
    private final SecurityActorAdapter securityActorAdapter;

    public PatientController(PatientService patientService, CaseService caseService, SecurityActorAdapter securityActorAdapter) {
        this.patientService = patientService;
        this.caseService = caseService;
        this.securityActorAdapter = securityActorAdapter;
    }

    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@RequestBody @Valid PatientCreateDTO patientDTO) {
        return ResponseEntity.ok(patientService.createPatient(patientDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatient(@PathVariable UUID id) {
        return patientService.getPatient(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/{id}/cases")
    public ResponseEntity<List<CaseDTO>> getCasesForPatient(@PathVariable UUID id) {
        return ResponseEntity.ok(caseService.getCasesForPatient(securityActorAdapter.currentUser(), id));
    }
}


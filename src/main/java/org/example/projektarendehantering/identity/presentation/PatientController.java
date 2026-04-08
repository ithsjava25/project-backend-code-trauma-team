package org.example.projektarendehantering.identity.presentation;

import jakarta.validation.Valid;
import org.example.projektarendehantering.cases.application.CaseService;
import org.example.projektarendehantering.identity.application.PatientCreateDTO;
import org.example.projektarendehantering.identity.application.PatientDTO;
import org.example.projektarendehantering.identity.application.PatientService;
import org.example.projektarendehantering.identity.infrastructure.SecurityActorAdapter;
import org.example.projektarendehantering.cases.application.CaseDTO;
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


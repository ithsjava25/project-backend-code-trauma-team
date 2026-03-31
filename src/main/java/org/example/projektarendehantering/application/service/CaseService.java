package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.CaseEntity;
import org.example.projektarendehantering.infrastructure.persistence.CaseNoteEntity;
import org.example.projektarendehantering.infrastructure.persistence.CaseNoteRepository;
import org.example.projektarendehantering.infrastructure.persistence.CaseRepository;
import org.example.projektarendehantering.infrastructure.persistence.PatientEntity;
import org.example.projektarendehantering.infrastructure.persistence.PatientRepository;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CaseService {

    private final CaseRepository caseRepository;
    private final CaseMapper caseMapper;
    private final PatientRepository patientRepository;
    private final CaseNoteRepository caseNoteRepository;

    public CaseService(CaseRepository caseRepository, CaseMapper caseMapper, PatientRepository patientRepository, CaseNoteRepository caseNoteRepository) {
        this.caseRepository = caseRepository;
        this.caseMapper = caseMapper;
        this.patientRepository = patientRepository;
        this.caseNoteRepository = caseNoteRepository;
    }

    @Transactional
    public void addNote(UUID caseId, String content, String author) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found"));

        CaseNoteEntity note = new CaseNoteEntity();
        note.setCaseEntity(caseEntity);
        note.setContent(content);
        note.setAuthor(author);
        note.setCreatedAt(Instant.now());

        caseNoteRepository.save(note);
    }

    @Transactional
    public CaseDTO createCase(CaseDTO caseDTO) {
        if (caseDTO.getPatientId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientId is required");
        }
        CaseEntity entity = caseMapper.toEntity(caseDTO);
        PatientEntity patient = patientRepository.findById(caseDTO.getPatientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        entity.setPatient(patient);
        if (entity.getStatus() == null) {
            entity.setStatus("OPEN");
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        CaseEntity savedEntity = caseRepository.save(entity);
        return caseMapper.toDTO(savedEntity);
    }

    @Transactional(readOnly = true)
    public Optional<CaseDTO> getCase(UUID id) {
        return caseRepository.findById(id).map(caseMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<CaseDTO> getAllCases() {
        return caseRepository.findAll().stream()
                .map(caseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CaseDTO> getCasesForPatient(UUID patientId) {
        return caseRepository.findAllByPatient_Id(patientId).stream()
                .map(caseMapper::toDTO)
                .collect(Collectors.toList());
    }
}

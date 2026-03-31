package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.CaseEntity;
import org.example.projektarendehantering.infrastructure.persistence.CaseRepository;
import org.example.projektarendehantering.infrastructure.persistence.PatientEntity;
import org.example.projektarendehantering.infrastructure.persistence.PatientRepository;
import org.example.projektarendehantering.presentation.dto.CaseAssignmentDTO;
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

    public CaseService(CaseRepository caseRepository, CaseMapper caseMapper, PatientRepository patientRepository) {
        this.caseRepository = caseRepository;
        this.caseMapper = caseMapper;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public CaseDTO createCase(Actor actor, CaseDTO caseDTO) {
        if (!canCreate(actor)) {
            throw new NotAuthorizedException("Not allowed to create cases");
        }
        if (caseDTO.getPatientId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientId is required");
        }
        CaseEntity entity = caseMapper.toEntity(caseDTO);
        PatientEntity patient = patientRepository.findById(caseDTO.getPatientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        entity.setPatient(patient);
        if (isDoctor(actor) || isManager(actor)) {
            entity.setOwnerId(actor.userId());
        }
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
    public Optional<CaseDTO> getCase(Actor actor, UUID id) {
        return caseRepository.findById(id)
                .map(entity -> {
                    requireCanRead(actor, entity);
                    return caseMapper.toDTO(entity);
                });
    }

    @Transactional(readOnly = true)
    public List<CaseDTO> getAllCases(Actor actor) {
        if (isManager(actor)) {
            return caseRepository.findAll().stream()
                    .map(caseMapper::toDTO)
                    .collect(Collectors.toList());
        }
        if (isDoctor(actor)) {
            return caseRepository.findAllByOwnerId(actor.userId()).stream()
                    .map(caseMapper::toDTO)
                    .collect(Collectors.toList());
        }
        if (isNurse(actor)) {
            return caseRepository.findAllByHandlerId(actor.userId()).stream()
                    .map(caseMapper::toDTO)
                    .collect(Collectors.toList());
        }
        if (isPatientReadOnly(actor)) {
            return caseRepository.findAllByOtherId(actor.userId()).stream()
                    .map(caseMapper::toDTO)
                    .collect(Collectors.toList());
        }
        throw new NotAuthorizedException("Not allowed to list cases");
    }

    @Transactional(readOnly = true)
    public List<CaseDTO> getCasesForPatient(Actor actor, UUID patientId) {
        return caseRepository.findAllByPatient_Id(patientId).stream()
                .peek(entity -> requireCanRead(actor, entity))
                .map(caseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CaseDTO assignUsers(Actor actor, UUID caseId, CaseAssignmentDTO dto) {
        if (!isManager(actor) && !isDoctor(actor)) {
            throw new NotAuthorizedException("Not allowed to assign users to case");
        }
        CaseEntity entity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found"));
        if (isDoctor(actor) && entity.getOwnerId() != null && !entity.getOwnerId().equals(actor.userId())) {
            throw new NotAuthorizedException("Not allowed to modify assignments for this case");
        }
        if (dto.getOwnerId() != null) entity.setOwnerId(dto.getOwnerId());
        if (dto.getHandlerId() != null) entity.setHandlerId(dto.getHandlerId());
        if (dto.getOtherId() != null) entity.setOtherId(dto.getOtherId());
        return caseMapper.toDTO(caseRepository.save(entity));
    }

    private void requireCanRead(Actor actor, CaseEntity entity) {
        if (isManager(actor)) return;
        if (isDoctor(actor) && actor.userId().equals(entity.getOwnerId())) return;
        if (isNurse(actor) && actor.userId().equals(entity.getHandlerId())) return;
        if (isPatientReadOnly(actor) && actor.userId().equals(entity.getOtherId())) return;
        throw new NotAuthorizedException("Not allowed to read this case");
    }

    private boolean canCreate(Actor actor) {
        return isManager(actor) || isDoctor(actor);
    }

    private boolean isManager(Actor actor) {
        return actor.role() == Role.MANAGER || actor.role() == Role.ADMIN;
    }

    private boolean isDoctor(Actor actor) {
        return actor.role() == Role.DOCTOR || actor.role() == Role.CASE_OWNER;
    }

    private boolean isNurse(Actor actor) {
        return actor.role() == Role.NURSE || actor.role() == Role.HANDLER;
    }

    private boolean isPatientReadOnly(Actor actor) {
        return actor.role() == Role.PATIENT || actor.role() == Role.OTHER;
    }
}

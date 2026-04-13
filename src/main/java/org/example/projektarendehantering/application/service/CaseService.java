package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.CaseEntity;
import org.example.projektarendehantering.infrastructure.persistence.CaseNoteEntity;
import org.example.projektarendehantering.infrastructure.persistence.CaseNoteRepository;
import org.example.projektarendehantering.infrastructure.persistence.CaseRepository;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeEntity;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeRepository;
import org.example.projektarendehantering.infrastructure.persistence.PatientEntity;
import org.example.projektarendehantering.infrastructure.persistence.PatientRepository;
import org.example.projektarendehantering.presentation.dto.CaseAssignmentDTO;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseRepository caseRepository;
    private final CaseMapper caseMapper;
    private final CaseNoteMapper caseNoteMapper;
    private final PatientRepository patientRepository;
    private final CaseNoteRepository caseNoteRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public void addNote(UUID caseId, String content, Actor actor) {
        if (actor == null) {
            throw new NotAuthorizedException("Not allowed to add notes");
        }
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found"));
        requireCanRead(actor, caseEntity);

        CaseNoteEntity note = caseNoteMapper.toEntity(actor, content);
        note.setCaseEntity(caseEntity);

        caseNoteRepository.save(note);
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
        if (isDoctor(actor)) {
            if (entity.getOwnerId() == null || !entity.getOwnerId().equals(actor.userId())) {
                throw new NotAuthorizedException("Not allowed to modify assignments for this case");
            }
            if (dto.getOwnerId() != null) {
                throw new NotAuthorizedException("Not allowed to change owner for this case");
            }
        }

        if (isManager(actor) && dto.getOwnerId() != null) {
            UUID ownerId = requireEmployeeWithRole(dto.getOwnerId(), Set.of(Role.DOCTOR), "ownerId");
            entity.setOwnerId(ownerId);
        }
        if (dto.getHandlerId() != null) {
            UUID handlerId = requireEmployeeWithRole(dto.getHandlerId(), Set.of(Role.NURSE), "handlerId");
            entity.setHandlerId(handlerId);
        }
        return caseMapper.toDTO(caseRepository.save(entity));
    }

    private UUID requireEmployeeWithRole(UUID id, Set<Role> allowedRoles, String fieldName) {
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        fieldName + " refers to a non-existent employee: " + id
                ));
        if (employee.getRole() == null || !allowedRoles.contains(employee.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must refer to an employee with role " + allowedRoles + " (was " + employee.getRole() + "): " + id
            );
        }
        return id;
    }

    private void requireCanRead(Actor actor, CaseEntity entity) {
        if (isManager(actor)) return;
        if (isDoctor(actor) && actor.userId().equals(entity.getOwnerId())) return;
        if (isNurse(actor) && actor.userId().equals(entity.getHandlerId())) return;
        throw new NotAuthorizedException("Not allowed to read this case");
    }

    private boolean canCreate(Actor actor) {
        return isManager(actor) || isDoctor(actor);
    }

    private boolean isManager(Actor actor) {
        return actor.role() == Role.MANAGER;
    }

    private boolean isDoctor(Actor actor) {
        return actor.role() == Role.DOCTOR;
    }

    private boolean isNurse(Actor actor) {
        return actor.role() == Role.NURSE;
    }
}

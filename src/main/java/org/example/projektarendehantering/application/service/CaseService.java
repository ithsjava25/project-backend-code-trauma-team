package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.BadRequestException;
import org.example.projektarendehantering.common.CaseStatus;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.AuditEventEntity;
import org.example.projektarendehantering.infrastructure.persistence.CaseEntity;
import org.example.projektarendehantering.infrastructure.persistence.CaseNoteEntity;
import org.example.projektarendehantering.infrastructure.persistence.CaseNoteRepository;
import org.example.projektarendehantering.infrastructure.persistence.CaseRepository;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeEntity;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeRepository;
import org.example.projektarendehantering.infrastructure.persistence.PatientEntity;
import org.example.projektarendehantering.infrastructure.persistence.PatientRepository;
import org.example.projektarendehantering.infrastructure.persistence.UserAccountEntity;
import org.example.projektarendehantering.infrastructure.persistence.UserAccountRepository;
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
    private final UserAccountRepository userAccountRepository;
    private final AuditService auditService;

    @Transactional
    public void addNote(UUID caseId, String content, Actor actor) {
        if (actor == null) {
            throw new NotAuthorizedException("Not allowed to add notes");
        }
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found"));
        if (caseEntity.getStatus() == CaseStatus.CLOSED) {
            throw new BadRequestException("Case is closed");
        }
        requireCanRead(actor, caseEntity);

        CaseNoteEntity note = caseNoteMapper.toEntity(actor, content);
        note.setId(UUID.randomUUID());
        note.setCaseEntity(caseEntity);

        caseNoteRepository.save(note);

        CaseStatus previousStatus = caseEntity.getStatus();
        caseEntity.setStatus(CaseStatus.COMMUNICATION);
        caseRepository.save(caseEntity);
        recordStatusChange(actor, caseEntity.getId(), previousStatus, CaseStatus.COMMUNICATION);
    }

    @Transactional
    public CaseDTO createCase(Actor actor, CaseDTO caseDTO) {
        if (!canCreate(actor)) {
            throw new NotAuthorizedException("Not allowed to create cases");
        }
        UUID requestedPatientId = caseDTO.getPatientId();
        if (isPatient(actor)) {
            if (requestedPatientId != null && !actor.userId().equals(requestedPatientId)) {
                throw new NotAuthorizedException("Patients can only create cases for themselves");
            }
            requestedPatientId = null;
        }
        if (requestedPatientId == null) {
            if (!isPatient(actor)) {
                throw new BadRequestException("patientId is required");
            }
        }
        CaseEntity entity = caseMapper.toEntity(caseDTO);
        entity.setId(UUID.randomUUID());
        PatientEntity patient;
        if (isPatient(actor)) {
            patient = patientRepository.findByUserAccount_Id(actor.userId()).orElseGet(() -> {
                UserAccountEntity account = userAccountRepository.findById(actor.userId())
                        .orElseThrow(() -> new NotAuthorizedException("Patient account not found"));
                PatientEntity self = new PatientEntity();
                self.setId(UUID.randomUUID());
                self.setUserAccount(account);
                self.setCreatedAt(Instant.now());
                return patientRepository.save(self);
            });
        } else {
            patient = patientRepository.findById(requestedPatientId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        }
        entity.setPatient(patient);
        if (isDoctor(actor) || isManager(actor)) {
            entity.setOwnerId(actor.userId());
        }
        entity.setStatus(CaseStatus.CREATED);

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        CaseEntity savedEntity = caseRepository.save(entity);
        recordStatusChange(actor, savedEntity.getId(), null, CaseStatus.CREATED);
        return caseMapper.toDTO(savedEntity);
    }

    @Transactional
    public CaseDTO updateCase(Actor actor, UUID caseId, CaseDTO caseDTO) {

        if (caseDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title and description are required");
        }

        String title = caseDTO.getTitle() == null ? null : caseDTO.getTitle().trim();
        String description = caseDTO.getDescription() == null ? null : caseDTO.getDescription().trim();

        if (title == null || title.isBlank()
                || description == null || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title and description are required");
        }
        if (title.length() > 200 || description.length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title/description length exceeds limits");
        }

        CaseEntity entity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found"));
        if (entity.getStatus() == CaseStatus.CLOSED) {
            throw new BadRequestException("Case is closed");
        }

        requireCanEdit(actor, entity);

        CaseStatus previousStatus = entity.getStatus();
        entity.setTitle(title);
        entity.setDescription(description);
        entity.setStatus(CaseStatus.UPDATED);

        CaseEntity savedEntity = caseRepository.save(entity);
        recordStatusChange(actor, savedEntity.getId(), previousStatus, CaseStatus.UPDATED);
        return caseMapper.toDTO(savedEntity);
    }

    @Transactional
    public void deleteCase(Actor actor, UUID caseId) {
        CaseEntity entity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found"));
        if (entity.getStatus() == CaseStatus.CLOSED) {
            throw new BadRequestException("Case is closed");
        }

        requireCanDelete(actor, entity);
        CaseStatus previousStatus = entity.getStatus();
        entity.setStatus(CaseStatus.CLOSED);
        caseRepository.save(entity);
        recordStatusChange(actor, entity.getId(), previousStatus, CaseStatus.CLOSED);
    }

    @Transactional(readOnly = true)
    public Optional<CaseDTO> getCase(Actor actor, UUID id) {
        Optional<CaseEntity> entityOpt = caseRepository.findById(id);
        if (entityOpt.isPresent() && entityOpt.get().getStatus() == CaseStatus.CLOSED && !isManager(actor)) {
            throw new BadRequestException("Case is closed");
        }
        return entityOpt.map(entity -> {
                    requireCanRead(actor, entity);
                    return caseMapper.toDTO(entity);
                });
    }

    @Transactional(readOnly = true)
    public List<CaseDTO> getClosedCases(Actor actor) {
        if (!isManager(actor)) {
            throw new NotAuthorizedException("Not allowed to view closed cases");
        }
        return caseRepository.findAllByStatus(CaseStatus.CLOSED).stream()
                .map(caseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CaseDTO> getAllCases(Actor actor) {
        if (isManager(actor)) {
            return caseRepository.findAllByStatusNot(CaseStatus.CLOSED).stream()
                    .map(caseMapper::toDTO)
                    .collect(Collectors.toList());
        }
        if (isDoctor(actor)) {
            return caseRepository.findAllByOwnerIdAndStatusNot(actor.userId(), CaseStatus.CLOSED).stream()
                    .map(caseMapper::toDTO)
                    .collect(Collectors.toList());
        }
        if (isNurse(actor)) {
            return caseRepository.findAllByHandlerIdAndStatusNot(actor.userId(), CaseStatus.CLOSED).stream()
                    .map(caseMapper::toDTO)
                    .collect(Collectors.toList());
        }
        if (isPatient(actor)) {
            return caseRepository.findAllByPatient_UserAccount_IdAndStatusNot(actor.userId(), CaseStatus.CLOSED).stream()
                    .map(caseMapper::toDTO)
                    .collect(Collectors.toList());
        }
        throw new NotAuthorizedException("Not allowed to list cases");
    }

    @Transactional(readOnly = true)
    public List<CaseDTO> getCasesForPatient(Actor actor, UUID patientId) {
        return caseRepository.findAllByPatient_IdAndStatusNot(patientId, CaseStatus.CLOSED).stream()
                .filter(entity -> canRead(actor, entity))
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
        if (entity.getStatus() == CaseStatus.CLOSED) {
            throw new BadRequestException("Case is closed");
        }

        if (isDoctor(actor)) {
            // Doctors can only modify if they are owner OR if it's unowned
            if (entity.getOwnerId() != null && !entity.getOwnerId().equals(actor.userId())) {
                throw new NotAuthorizedException("Not allowed to modify assignments for this case");
            }
        }

        if(dto.getOwnerId() == null && dto.getHandlerId() == null) {
            throw new BadRequestException("At least one of ownerId or handlerId must be provided.");
        }

        if (dto.getOwnerId() != null) {
            UUID ownerId = requireEmployeeWithRole(dto.getOwnerId(), Set.of(Role.DOCTOR), "ownerId");
            entity.setOwnerId(ownerId);
        }
        if (dto.getHandlerId() != null) {
            UUID handlerId = requireEmployeeWithRole(dto.getHandlerId(), Set.of(Role.NURSE), "handlerId");
            entity.setHandlerId(handlerId);
        }

        CaseStatus previousStatus = entity.getStatus();
        if (previousStatus != CaseStatus.ASSIGNED) {
            entity.setStatus(CaseStatus.ASSIGNED);
            CaseEntity savedEntity = caseRepository.save(entity);
            recordStatusChange(actor, savedEntity.getId(), previousStatus, CaseStatus.ASSIGNED);
            return caseMapper.toDTO(savedEntity);
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
        if (isPatient(actor) && isActorPatientLinked(actor, entity)) return;
        throw new NotAuthorizedException("Not allowed to read this case");
    }

    private void requireCanEdit(Actor actor, CaseEntity entity) {
        if (actor == null) {
            throw new NotAuthorizedException("Not allowed to edit this case");
        }
        if (isManager(actor)) return;
        if (isDoctor(actor) && actor.userId().equals(entity.getOwnerId())) return;
        throw new NotAuthorizedException("Not allowed to edit this case");
    }

    private void requireCanDelete(Actor actor, CaseEntity entity) {
        if (actor == null) {
            throw new NotAuthorizedException("Not allowed to delete this case");
        }
        if (isManager(actor)) return;
        if (isDoctor(actor) && actor.userId().equals(entity.getOwnerId())) return;
        throw new NotAuthorizedException("Not allowed to delete this case");
    }

    private boolean canCreate(Actor actor) {
        return isManager(actor) || isDoctor(actor) || isPatient(actor);
    }

    private boolean canRead(Actor actor, CaseEntity entity) {
        if (isManager(actor)) return true;
        if (isDoctor(actor) && actor.userId().equals(entity.getOwnerId())) return true;
        if (isNurse(actor) && actor.userId().equals(entity.getHandlerId())) return true;
        if (isPatient(actor) && isActorPatientLinked(actor, entity)) return true;
        return false;
    }

    private boolean isActorPatientLinked(Actor actor, CaseEntity entity) {
        return entity.getPatient() != null
                && entity.getPatient().getUserAccount() != null
                && actor.userId().equals(entity.getPatient().getUserAccount().getId());
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

    private boolean isPatient(Actor actor) {
        return actor.role() == Role.PATIENT;
    }

    private void recordStatusChange(Actor actor, UUID caseId, CaseStatus from, CaseStatus to) {
        String statusChange = (from != null ? from.name() : "NEW") + " -> " + to.name();
        String eventName = from == null ? "CASE_CREATED" : "STATUS_CHANGED";
        String description = from == null ? "Case was created" : "Status changed from " + from + " to " + to;

        AuditEventEntity event = AuditEventEntity.builder()
                .caseId(caseId)
                .statusChange(statusChange)
                .eventName(eventName)
                .description(description)
                .actorId(actor != null ? actor.userId() : null)
                .actorRole(actor != null && actor.role() != null ? actor.role().name() : null)
                .occurredAt(Instant.now())
                .build();
        auditService.record(event);
    }
}

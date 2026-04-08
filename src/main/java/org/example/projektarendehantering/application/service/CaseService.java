package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.*;
import org.example.projektarendehantering.presentation.dto.CaseAssignmentDTO;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CaseService {

    private final CaseRepository caseRepository;
    private final CaseMapper caseMapper;
    private final PatientProfileRepository patientProfileRepository;
    private final CaseNoteRepository caseNoteRepository;
    private final AccountRepository accountRepository;
    private final CaseNoteMapper caseNoteMapper;

    public CaseService(CaseRepository caseRepository, CaseMapper caseMapper, PatientProfileRepository patientProfileRepository, CaseNoteRepository caseNoteRepository, AccountRepository accountRepository, CaseNoteMapper caseNoteMapper) {
        this.caseRepository = caseRepository;
        this.caseMapper = caseMapper;
        this.patientProfileRepository = patientProfileRepository;
        this.caseNoteRepository = caseNoteRepository;
        this.accountRepository = accountRepository;
        this.caseNoteMapper = caseNoteMapper;
    }

    @Transactional
    public void addNote(UUID caseId, String content, Actor actor) {
        if (actor == null) throw new NotAuthorizedException("Not allowed to add notes");
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found"));

        CaseNoteEntity note = caseNoteMapper.toEntity(actor, content);
        note.setCaseEntity(caseEntity);
        caseNoteRepository.save(note);
    }

    @Transactional
    public CaseDTO createCase(Actor actor, CaseDTO caseDTO) {
        if (!canCreate(actor)) throw new NotAuthorizedException("Not allowed to create cases");
        if (caseDTO.getPatientId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientId is required");
        
        CaseEntity entity = caseMapper.toEntity(caseDTO);
        PatientProfileEntity subject = patientProfileRepository.findById(caseDTO.getPatientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient Profile not found"));
        entity.setSubject(subject);
        
        if (isDoctor(actor) || isManager(actor)) entity.setOwnerId(actor.userId());
        if (entity.getStatus() == null) entity.setStatus("OPEN");
        if (entity.getCreatedAt() == null) entity.setCreatedAt(Instant.now());
        
        CaseEntity savedEntity = caseRepository.save(entity);
        return caseMapper.toDTO(savedEntity);
    }

    @Transactional(readOnly = true)
    public Optional<CaseDTO> getCase(Actor actor, UUID id) {
        return caseRepository.findById(id).map(entity -> {
            requireCanRead(actor, entity);
            return caseMapper.toDTO(entity);
        });
    }

    @Transactional(readOnly = true)
    public List<CaseDTO> getAllCases(Actor actor) {
        if (isManager(actor)) return caseRepository.findAll().stream().map(caseMapper::toDTO).collect(Collectors.toList());
        if (isDoctor(actor)) return caseRepository.findAllByOwnerId(actor.userId()).stream().map(caseMapper::toDTO).collect(Collectors.toList());
        if (isNurse(actor)) return caseRepository.findAllByHandlerId(actor.userId()).stream().map(caseMapper::toDTO).collect(Collectors.toList());
        if (isPatient(actor)) {
            Map<UUID, CaseEntity> byId = new LinkedHashMap<>();
            caseRepository.findAllBySubject_Id(actor.userId()).forEach(c -> byId.putIfAbsent(c.getId(), c));
            caseRepository.findAllByOtherId(actor.userId()).forEach(c -> byId.putIfAbsent(c.getId(), c));
            return byId.values().stream().map(caseMapper::toDTO).collect(Collectors.toList());
        }
        throw new NotAuthorizedException("Not allowed to list cases");
    }

    @Transactional(readOnly = true)
    public List<CaseDTO> getCasesForPatient(Actor actor, UUID patientId) {
        return caseRepository.findAllBySubject_Id(patientId).stream()
                .peek(entity -> requireCanRead(actor, entity))
                .map(caseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CaseDTO assignUsers(Actor actor, UUID caseId, CaseAssignmentDTO dto) {
        if (!isManager(actor) && !isDoctor(actor)) throw new NotAuthorizedException("Not allowed to assign users to case");
        CaseEntity entity = caseRepository.findById(caseId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found"));
        
        if (isDoctor(actor)) {
            if (entity.getOwnerId() == null || !entity.getOwnerId().equals(actor.userId())) throw new NotAuthorizedException("Not allowed to modify assignments");
            if (dto.getOwnerId() != null) throw new NotAuthorizedException("Not allowed to change owner");
        }

        if (isManager(actor) && dto.getOwnerId() != null) entity.setOwnerId(requireAccountWithRole(dto.getOwnerId(), Set.of(Role.DOCTOR), "ownerId"));
        if (dto.getHandlerId() != null) entity.setHandlerId(requireAccountWithRole(dto.getHandlerId(), Set.of(Role.NURSE), "handlerId"));
        if (dto.getOtherId() != null) entity.setOtherId(requireAccountWithRole(dto.getOtherId(), Set.of(Role.PATIENT), "otherId"));
        
        return caseMapper.toDTO(caseRepository.save(entity));
    }

    private UUID requireAccountWithRole(UUID id, Set<Role> allowedRoles, String fieldName) {
        AccountEntity account = accountRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " refers to non-existent user"));
        if (!allowedRoles.contains(account.getRole())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must have role " + allowedRoles);
        return id;
    }

    private void requireCanRead(Actor actor, CaseEntity entity) {
        if (isManager(actor)) return;
        if (isDoctor(actor) && actor.userId().equals(entity.getOwnerId())) return;
        if (isNurse(actor) && actor.userId().equals(entity.getHandlerId())) return;
        if (isPatient(actor) && entity.getSubject() != null && actor.userId().equals(entity.getSubject().getId())) return;
        if (isPatient(actor) && actor.userId().equals(entity.getOtherId())) return;
        throw new NotAuthorizedException("Not allowed to read this case");
    }

    private boolean canCreate(Actor actor) { return isManager(actor) || isDoctor(actor); }
    private boolean isManager(Actor actor) { return actor.role() == Role.MANAGER; }
    private boolean isDoctor(Actor actor) { return actor.role() == Role.DOCTOR; }
    private boolean isNurse(Actor actor) { return actor.role() == Role.NURSE; }
    private boolean isPatient(Actor actor) { return actor.role() == Role.PATIENT; }
}

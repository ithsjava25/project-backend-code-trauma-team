package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.BadRequestException;
import org.example.projektarendehantering.common.CaseStatus;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.*;
import org.example.projektarendehantering.presentation.dto.CaseAssignmentDTO;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    private CaseRepository caseRepository;
    @Mock
    private CaseMapper caseMapper;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private CaseNoteRepository caseNoteRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private CaseNoteMapper caseNoteMapper;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private CaseService caseService;

    private Actor doctorActor;
    private Actor nurseActor;
    private Actor managerActor;
    private Actor otherDoctorActor;
    private UUID caseId;
    private CaseEntity caseEntity;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        UUID doctorId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();
        UUID otherDoctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();

        doctorActor = new Actor(doctorId, Role.DOCTOR, "Doctor", "doctor_user");
        nurseActor = new Actor(nurseId, Role.NURSE, "Nurse", "nurse_user");
        managerActor = new Actor(managerId, Role.MANAGER, "Manager", "manager_user");
        otherDoctorActor = new Actor(otherDoctorId, Role.DOCTOR, "Other Doctor", "other_doctor_user");

        caseId = UUID.randomUUID();
        caseEntity = new CaseEntity();
        caseEntity.setId(caseId);
        caseEntity.setOwnerId(doctorId);
        caseEntity.setHandlerId(nurseId);
        
        PatientEntity patient = new PatientEntity();
        patient.setId(patientId);
        caseEntity.setPatient(patient);
    }

    @Test
    void getCase_shouldAllowOwnerToRead() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(caseMapper.toDTO(caseEntity)).thenReturn(new CaseDTO());

        CaseDTO result = caseService.getCase(doctorActor, caseId).orElseThrow();

        assertThat(result).isNotNull();
        verify(caseRepository).findById(caseId);
    }

    @Test
    void getCase_shouldAllowHandlerToRead() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(caseMapper.toDTO(caseEntity)).thenReturn(new CaseDTO());

        CaseDTO result = caseService.getCase(nurseActor, caseId).orElseThrow();

        assertThat(result).isNotNull();
    }

    @Test
    void getCase_shouldAllowManagerToRead() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(caseMapper.toDTO(caseEntity)).thenReturn(new CaseDTO());

        CaseDTO result = caseService.getCase(managerActor, caseId).orElseThrow();

        assertThat(result).isNotNull();
    }

    @Test
    void getCase_shouldDenyUnauthorizedAccess() {
        Actor unauthorizedActor = new Actor(UUID.randomUUID(), Role.DOCTOR, "Unauthorized", "unauthorized_user");
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> caseService.getCase(unauthorizedActor, caseId))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessageContaining("Not allowed to read this case");
    }

    @Test
    void createCase_shouldAllowDoctor() {
        CaseDTO dto = new CaseDTO();
        dto.setPatientId(patientId);
        PatientEntity patient = new PatientEntity();
        patient.setId(patientId);

        when(caseMapper.toEntity(dto)).thenReturn(new CaseEntity());
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(caseRepository.save(any(CaseEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(caseMapper.toDTO(any(CaseEntity.class))).thenReturn(new CaseDTO());

        caseService.createCase(doctorActor, dto);

        verify(caseRepository).save(any(CaseEntity.class));
        verify(auditService).record(argThat(e -> "NEW -> CREATED".equals(e.getStatusChange())));
    }

    @Test
    void createCase_shouldDenyNurse() {
        CaseDTO dto = new CaseDTO();
        assertThatThrownBy(() -> caseService.createCase(nurseActor, dto))
                .isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    void updateCase_shouldAllowOwnerDoctor() {
        CaseDTO updateDto = new CaseDTO();
        updateDto.setTitle("Updated title");
        updateDto.setDescription("Updated description");

        CaseDTO mappedDto = new CaseDTO();
        mappedDto.setId(caseId);
        mappedDto.setTitle("Updated title");
        mappedDto.setDescription("Updated description");

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(caseRepository.save(caseEntity)).thenReturn(caseEntity);
        when(caseMapper.toDTO(caseEntity)).thenReturn(mappedDto);

        CaseDTO result = caseService.updateCase(doctorActor, caseId, updateDto);

        assertThat(result.getTitle()).isEqualTo("Updated title");
        assertThat(caseEntity.getTitle()).isEqualTo("Updated title");
        assertThat(caseEntity.getDescription()).isEqualTo("Updated description");
        verify(caseRepository).save(caseEntity);
        verify(auditService).record(argThat(e -> e.getStatusChange() != null && e.getStatusChange().endsWith("-> UPDATED")));
    }

    @Test
    void updateCase_shouldAllowManager() {
        CaseDTO updateDto = new CaseDTO();
        updateDto.setTitle("Manager updated title");
        updateDto.setDescription("Manager updated description");

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(caseRepository.save(caseEntity)).thenReturn(caseEntity);
        when(caseMapper.toDTO(caseEntity)).thenReturn(new CaseDTO());

        caseService.updateCase(managerActor, caseId, updateDto);

        assertThat(caseEntity.getTitle()).isEqualTo("Manager updated title");
        assertThat(caseEntity.getDescription()).isEqualTo("Manager updated description");
        verify(caseRepository).save(caseEntity);
    }

    @Test
    void updateCase_shouldDenyNonOwnerDoctor() {
        CaseDTO updateDto = new CaseDTO();
        updateDto.setTitle("Nope");
        updateDto.setDescription("Still nope");

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> caseService.updateCase(otherDoctorActor, caseId, updateDto))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessageContaining("Not allowed to edit this case");

        verify(caseRepository, never()).save(any(CaseEntity.class));
    }

    @Test
    void updateCase_shouldDenyNurse() {
        CaseDTO updateDto = new CaseDTO();
        updateDto.setTitle("Nope");
        updateDto.setDescription("Still nope");

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> caseService.updateCase(nurseActor, caseId, updateDto))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessageContaining("Not allowed to edit this case");

        verify(caseRepository, never()).save(any(CaseEntity.class));
    }

    @Test
    void updateCase_shouldReturnNotFoundWhenCaseDoesNotExist() {
        CaseDTO updateDto = new CaseDTO();
        updateDto.setTitle("Updated title");
        updateDto.setDescription("Updated description");

        when(caseRepository.findById(caseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseService.updateCase(doctorActor, caseId, updateDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND")
                .hasMessageContaining("Case not found");

        verify(caseRepository, never()).save(any(CaseEntity.class));
    }

    @Test
    void deleteCase_shouldAllowOwnerDoctor() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        caseService.deleteCase(doctorActor, caseId);

        assertThat(caseEntity.getStatus()).isEqualTo(CaseStatus.CLOSED);
        verify(caseRepository).save(caseEntity);
        verify(auditService).record(argThat(e -> e.getStatusChange() != null && e.getStatusChange().endsWith("-> CLOSED")));
    }

    @Test
    void deleteCase_shouldAllowManager() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        caseService.deleteCase(managerActor, caseId);

        assertThat(caseEntity.getStatus()).isEqualTo(CaseStatus.CLOSED);
        verify(caseRepository).save(caseEntity);
        verify(auditService).record(argThat(e -> e.getStatusChange() != null && e.getStatusChange().endsWith("-> CLOSED")));
    }

    @Test
    void deleteCase_shouldDenyNonOwnerDoctor() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> caseService.deleteCase(otherDoctorActor, caseId))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessageContaining("Not allowed to delete this case");

        assertThat(caseEntity.getStatus()).isNotEqualTo(CaseStatus.CLOSED);
        verify(caseRepository, never()).save(any(CaseEntity.class));
    }

    @Test
    void deleteCase_shouldDenyNurse() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> caseService.deleteCase(nurseActor, caseId))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessageContaining("Not allowed to delete this case");

        assertThat(caseEntity.getStatus()).isNotEqualTo(CaseStatus.CLOSED);
        verify(caseRepository, never()).save(any(CaseEntity.class));
    }

    @Test
    void deleteCase_shouldReturnNotFoundWhenCaseDoesNotExist() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseService.deleteCase(doctorActor, caseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND")
                .hasMessageContaining("Case not found");

        verify(caseRepository, never()).save(any(CaseEntity.class));
    }

    @Test
    void createCase_shouldRecordStatusChangeAuditWithCaseId() {
        CaseDTO dto = new CaseDTO();
        dto.setPatientId(patientId);
        PatientEntity patient = new PatientEntity();
        patient.setId(patientId);

        CaseEntity savedEntity = new CaseEntity();
        savedEntity.setId(caseId);

        when(caseMapper.toEntity(dto)).thenReturn(new CaseEntity());
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(caseRepository.save(any(CaseEntity.class))).thenReturn(savedEntity);
        when(caseMapper.toDTO(any(CaseEntity.class))).thenReturn(new CaseDTO());

        caseService.createCase(doctorActor, dto);

        verify(auditService).record(argThat(e ->
                "NEW -> CREATED".equals(e.getStatusChange()) &&
                caseId.equals(e.getCaseId()) &&
                doctorActor.userId().equals(e.getActorId())
        ));
    }

    @Test
    void updateCase_shouldRecordPreviousStatusInAudit() {
        caseEntity.setStatus(CaseStatus.ASSIGNED);
        CaseDTO updateDto = new CaseDTO();
        updateDto.setTitle("New title");
        updateDto.setDescription("New description");

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(caseRepository.save(caseEntity)).thenReturn(caseEntity);
        when(caseMapper.toDTO(caseEntity)).thenReturn(new CaseDTO());

        caseService.updateCase(doctorActor, caseId, updateDto);

        verify(auditService).record(argThat(e ->
                "ASSIGNED -> UPDATED".equals(e.getStatusChange()) &&
                caseId.equals(e.getCaseId())
        ));
    }

    @Test
    void deleteCase_shouldNotRecordAuditOnAuthorizationFailure() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> caseService.deleteCase(nurseActor, caseId))
                .isInstanceOf(NotAuthorizedException.class);

        verify(auditService, never()).record(any());
    }

    // --- Closed-case state conflict (409) ---

    @Test
    void updateCase_shouldReturnConflictWhenCaseIsClosed() {
        caseEntity.setStatus(CaseStatus.CLOSED);
        CaseDTO updateDto = new CaseDTO();
        updateDto.setTitle("Updated title");
        updateDto.setDescription("Updated description");

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> caseService.updateCase(doctorActor, caseId, updateDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Case is closed");

        verify(caseRepository, never()).save(any(CaseEntity.class));
    }

    @Test
    void deleteCase_shouldReturnConflictWhenCaseIsClosed() {
        caseEntity.setStatus(CaseStatus.CLOSED);
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> caseService.deleteCase(doctorActor, caseId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Case is closed");

        verify(caseRepository, never()).save(any(CaseEntity.class));
    }

    @Test
    void addNote_shouldReturnConflictWhenCaseIsClosed() {
        caseEntity.setStatus(CaseStatus.CLOSED);
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> caseService.addNote(caseId, "Some note", doctorActor))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Case is closed");

        verify(caseNoteRepository, never()).save(any());
        verify(caseRepository, never()).save(any(CaseEntity.class));
    }

    // --- Manager visibility of closed cases ---

    @Test
    void getCase_shouldAllowManagerToReadClosedCase() {
        caseEntity.setStatus(CaseStatus.CLOSED);
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(caseMapper.toDTO(caseEntity)).thenReturn(new CaseDTO());

        Optional<CaseDTO> result = caseService.getCase(managerActor, caseId);

        assertThat(result).isPresent();
    }

    @Test
    void getCase_shouldReturnEmptyForNonManagerOnClosedCase() {
        caseEntity.setStatus(CaseStatus.CLOSED);
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> caseService.getCase(doctorActor, caseId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Case is closed");
    }

    // --- getClosedCases ---

    @Test
    void getClosedCases_shouldReturnListForManager() {
        CaseEntity closedCase = new CaseEntity();
        closedCase.setId(UUID.randomUUID());
        closedCase.setStatus(CaseStatus.CLOSED);

        when(caseRepository.findAllByStatus(CaseStatus.CLOSED)).thenReturn(List.of(closedCase));
        when(caseMapper.toDTO(closedCase)).thenReturn(new CaseDTO());

        List<CaseDTO> result = caseService.getClosedCases(managerActor);

        assertThat(result).hasSize(1);
        verify(caseRepository).findAllByStatus(CaseStatus.CLOSED);
    }

    @Test
    void getClosedCases_shouldDenyDoctor() {
        assertThatThrownBy(() -> caseService.getClosedCases(doctorActor))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessageContaining("Not allowed to view closed cases");
    }

    @Test
    void assignUsers_shouldAllowDoctorToAssignThemselvesToUnownedCase() {
        caseEntity.setOwnerId(null); // Unowned
        CaseAssignmentDTO dto = new CaseAssignmentDTO();
        dto.setOwnerId(doctorActor.userId());

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(employeeRepository.findById(doctorActor.userId())).thenReturn(Optional.of(new EmployeeEntity(doctorActor.userId(), "Doctor", "doctor_user", Role.DOCTOR, Instant.now())));
        when(caseRepository.save(caseEntity)).thenReturn(caseEntity);
        when(caseMapper.toDTO(caseEntity)).thenReturn(new CaseDTO());

        caseService.assignUsers(doctorActor, caseId, dto);

        assertThat(caseEntity.getOwnerId()).isEqualTo(doctorActor.userId());
        verify(caseRepository).save(caseEntity);
    }

    @Test
    void assignUsers_shouldAllowDoctorToTransferOwnershipOfOwnedCase() {
        // doctorActor already owns it (set in setUp)
        CaseAssignmentDTO dto = new CaseAssignmentDTO();
        dto.setOwnerId(otherDoctorActor.userId());

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(employeeRepository.findById(otherDoctorActor.userId())).thenReturn(Optional.of(new EmployeeEntity(otherDoctorActor.userId(), "Other Doctor", "other_doctor_user", Role.DOCTOR, Instant.now())));
        when(caseRepository.save(caseEntity)).thenReturn(caseEntity);
        when(caseMapper.toDTO(caseEntity)).thenReturn(new CaseDTO());

        caseService.assignUsers(doctorActor, caseId, dto);

        assertThat(caseEntity.getOwnerId()).isEqualTo(otherDoctorActor.userId());
        verify(caseRepository).save(caseEntity);
    }

    @Test
    void assignUsers_shouldDenyDoctorToModifyAssignmentsOfCaseOwnedByOther() {
        caseEntity.setOwnerId(otherDoctorActor.userId());
        CaseAssignmentDTO dto = new CaseAssignmentDTO();
        dto.setOwnerId(doctorActor.userId());

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> caseService.assignUsers(doctorActor, caseId, dto))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessageContaining("Not allowed to modify assignments for this case");
    }

    @Test
    void assignUsers_shouldAllowManagerToAssignAnyDoctor() {
        CaseAssignmentDTO dto = new CaseAssignmentDTO();
        dto.setOwnerId(otherDoctorActor.userId());

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(employeeRepository.findById(otherDoctorActor.userId())).thenReturn(Optional.of(new EmployeeEntity(otherDoctorActor.userId(), "Other Doctor", "other_doctor_user", Role.DOCTOR, Instant.now())));
        when(caseRepository.save(caseEntity)).thenReturn(caseEntity);
        when(caseMapper.toDTO(caseEntity)).thenReturn(new CaseDTO());

        caseService.assignUsers(managerActor, caseId, dto);

        assertThat(caseEntity.getOwnerId()).isEqualTo(otherDoctorActor.userId());
        verify(caseRepository).save(caseEntity);
    }

    @Test
    void assignUsers_shouldSetStatusToHandlerAssigned() {
        caseEntity.setStatus(CaseStatus.CREATED);
        CaseAssignmentDTO dto = new CaseAssignmentDTO();
        dto.setHandlerId(nurseActor.userId());

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseEntity));
        when(employeeRepository.findById(nurseActor.userId())).thenReturn(Optional.of(new EmployeeEntity(nurseActor.userId(), "Nurse", "nurse_user", Role.NURSE, Instant.now())));
        when(caseRepository.save(caseEntity)).thenReturn(caseEntity);
        when(caseMapper.toDTO(caseEntity)).thenReturn(new CaseDTO());

        caseService.assignUsers(managerActor, caseId, dto);

        assertThat(caseEntity.getStatus()).isEqualTo(CaseStatus.ASSIGNED);
        verify(auditService).record(argThat(e -> "CREATED -> ASSIGNED".equals(e.getStatusChange())));
    }

}

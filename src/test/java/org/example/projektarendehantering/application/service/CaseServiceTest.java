package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.*;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private CaseService caseService;

    private Actor doctorActor;
    private Actor nurseActor;
    private Actor managerActor;
    private UUID caseId;
    private CaseEntity caseEntity;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        UUID doctorId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();
        patientId = UUID.randomUUID();

        doctorActor = new Actor(doctorId, Role.DOCTOR, "Doctor", "doctor_user");
        nurseActor = new Actor(nurseId, Role.NURSE, "Nurse", "nurse_user");
        managerActor = new Actor(managerId, Role.MANAGER, "Manager", "manager_user");

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
    }

    @Test
    void createCase_shouldDenyNurse() {
        CaseDTO dto = new CaseDTO();
        assertThatThrownBy(() -> caseService.createCase(nurseActor, dto))
                .isInstanceOf(NotAuthorizedException.class);
    }
}

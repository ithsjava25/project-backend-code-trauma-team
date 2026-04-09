package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.AuditEventEntity;
import org.example.projektarendehantering.infrastructure.persistence.AuditEventRepository;
import org.example.projektarendehantering.infrastructure.persistence.CaseEntity;
import org.example.projektarendehantering.infrastructure.persistence.CaseRepository;
import org.example.projektarendehantering.presentation.dto.AuditEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEventRepository auditEventRepository;
    @Mock
    private AuditEventMapper auditEventMapper;
    @Mock
    private CaseRepository caseRepository;

    @InjectMocks
    private AuditService auditService;

    private Actor managerActor;
    private Actor doctorActor;
    private UUID caseId;

    @BeforeEach
    void setUp() {
        managerActor = new Actor(UUID.randomUUID(), Role.MANAGER, "Manager", "manager");
        doctorActor = new Actor(UUID.randomUUID(), Role.DOCTOR, "Doctor", "doctor");
        caseId = UUID.randomUUID();
    }

    @Test
    void listEvents_shouldAllowManagerToSeeAll() {
        Page<AuditEventEntity> page = new PageImpl<>(List.of(new AuditEventEntity()));
        when(auditEventRepository.findAllByOccurredAtBetweenOrderByOccurredAtDesc(any(), any(), any()))
                .thenReturn(page);
        when(auditEventMapper.toDTO(any())).thenReturn(new AuditEventDTO());

        Page<AuditEventDTO> result = auditService.listEvents(managerActor, null, null, null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void listEvents_shouldAllowDoctorToSeeOnlyTheirCases() {
        CaseEntity caseEntity = new CaseEntity();
        caseEntity.setId(caseId);
        
        when(caseRepository.findAllByOwnerId(doctorActor.userId())).thenReturn(List.of(caseEntity));
        when(auditEventRepository.findAllByCaseIdInAndOccurredAtBetweenOrderByOccurredAtDesc(eq(Set.of(caseId)), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(new AuditEventEntity())));
        when(auditEventMapper.toDTO(any())).thenReturn(new AuditEventDTO());

        Page<AuditEventDTO> result = auditService.listEvents(doctorActor, null, null, null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void listEvents_shouldDenyDoctorAccessToUnownedCase() {
        when(caseRepository.findAllByOwnerId(doctorActor.userId())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> auditService.listEvents(doctorActor, null, null, caseId, Pageable.unpaged()))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessageContaining("Not allowed to view audit events for this case");
    }

    @Test
    void listEvents_shouldDenyPatient() {
        Actor patientActor = new Actor(UUID.randomUUID(), Role.PATIENT, "Patient", "patient");

        assertThatThrownBy(() -> auditService.listEvents(patientActor, null, null, null, Pageable.unpaged()))
                .isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    void listEvents_shouldThrowOnInvalidRange() {
        Instant from = Instant.now();
        Instant to = from.minusSeconds(10);

        assertThatThrownBy(() -> auditService.listEvents(managerActor, from, to, null, Pageable.unpaged()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid time range");
    }
}

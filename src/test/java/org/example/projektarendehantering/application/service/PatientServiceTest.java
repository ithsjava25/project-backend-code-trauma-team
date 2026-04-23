package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.ConflictException;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.PatientEntity;
import org.example.projektarendehantering.infrastructure.persistence.PatientRepository;
import org.example.projektarendehantering.presentation.dto.PatientCreateDTO;
import org.example.projektarendehantering.presentation.dto.PatientDTO;
import org.example.projektarendehantering.presentation.dto.PatientUpdateDTO;
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
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientService patientService;

    private Actor managerActor;
    private Actor doctorActor;

    @BeforeEach
    void setUp() {
        managerActor = new Actor(UUID.randomUUID(), Role.MANAGER, "Manager", "mgr");
        doctorActor = new Actor(UUID.randomUUID(), Role.DOCTOR, "Doctor", "doc");
    }

    @Test
    void createPatient_shouldSaveAndReturnDTO() {
        PatientCreateDTO createDto = new PatientCreateDTO();
        createDto.setPersonalIdentityNumber("19900101-1234");
        PatientEntity entity = new PatientEntity();
        entity.setPersonalIdentityNumber("19900101-1234");
        PatientDTO expectedDto = new PatientDTO();

        when(patientMapper.toEntity(createDto)).thenReturn(entity);
        when(patientRepository.findByPersonalIdentityNumber("19900101-1234")).thenReturn(Optional.empty());
        when(patientRepository.save(any(PatientEntity.class))).thenReturn(entity);
        when(patientMapper.toDTO(entity)).thenReturn(expectedDto);

        PatientDTO result = patientService.createPatient(createDto);

        assertThat(result).isEqualTo(expectedDto);
        verify(patientRepository).save(entity);
    }

    @Test
    void createPatient_shouldThrowConflictIfPinExists() {
        PatientCreateDTO createDto = new PatientCreateDTO();
        createDto.setPersonalIdentityNumber("19900101-1234");
        PatientEntity entity = new PatientEntity();
        entity.setPersonalIdentityNumber("19900101-1234");

        when(patientMapper.toEntity(createDto)).thenReturn(entity);
        when(patientRepository.findByPersonalIdentityNumber("19900101-1234")).thenReturn(Optional.of(new PatientEntity()));

        assertThatThrownBy(() -> patientService.createPatient(createDto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updatePatient_shouldUpdateAndReturnDTO() {
        UUID id = UUID.randomUUID();
        PatientUpdateDTO updateDto = new PatientUpdateDTO();
        updateDto.setPersonalIdentityNumber("19900101-1234");

        PatientEntity entity = new PatientEntity();
        entity.setPersonalIdentityNumber("19900101-1111");

        PatientDTO expectedDto = new PatientDTO();

        when(patientRepository.findById(id)).thenReturn(Optional.of(entity));
        when(patientRepository.findByPersonalIdentityNumber("19900101-1234")).thenReturn(Optional.empty());
        when(patientRepository.save(entity)).thenReturn(entity);
        when(patientMapper.toDTO(entity)).thenReturn(expectedDto);

        PatientDTO result = patientService.updatePatient(managerActor, id, updateDto);

        assertThat(result).isEqualTo(expectedDto);
        verify(patientMapper).updateEntity(updateDto, entity);
    }

    @Test
    void updatePatient_shouldThrowIfForbidden() {
        UUID id = UUID.randomUUID();
        PatientUpdateDTO updateDto = new PatientUpdateDTO();

        assertThatThrownBy(() -> patientService.updatePatient(doctorActor, id, updateDto))
                .isInstanceOf(NotAuthorizedException.class);
    }

}

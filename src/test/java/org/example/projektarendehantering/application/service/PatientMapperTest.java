package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.PatientEntity;
import org.example.projektarendehantering.presentation.dto.PatientCreateDTO;
import org.example.projektarendehantering.presentation.dto.PatientDTO;
import org.example.projektarendehantering.presentation.dto.PatientUpdateDTO;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PatientMapperTest {

    private final PatientMapper patientMapper = new PatientMapper();

    @Test
    void toDTO_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        PatientEntity entity = PatientEntity.builder()
                .id(id)
                .firstName("John")
                .lastName("Doe")
                .personalIdentityNumber("19900101-1234")
                .createdAt(now)
                .build();

        PatientDTO dto = patientMapper.toDTO(entity);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getPersonalIdentityNumber()).isEqualTo("19900101-1234");
        assertThat(dto.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_shouldMapAllFields() {
        PatientCreateDTO dto = PatientCreateDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .personalIdentityNumber("19910101-5678")
                .build();

        PatientEntity entity = patientMapper.toEntity(dto);

        assertThat(entity.getFirstName()).isEqualTo("Jane");
        assertThat(entity.getLastName()).isEqualTo("Smith");
        assertThat(entity.getPersonalIdentityNumber()).isEqualTo("19910101-5678");
    }

    @Test
    void updateEntity_shouldUpdateAllFields() {
        PatientEntity entity = PatientEntity.builder()
                .firstName("OldFirst")
                .lastName("OldLast")
                .personalIdentityNumber("19800101-0000")
                .build();

        PatientUpdateDTO dto = PatientUpdateDTO.builder()
                .firstName("NewFirst")
                .lastName("NewLast")
                .personalIdentityNumber("19800101-1111")
                .build();

        patientMapper.updateEntity(dto, entity);

        assertThat(entity.getFirstName()).isEqualTo("NewFirst");
        assertThat(entity.getLastName()).isEqualTo("NewLast");
        assertThat(entity.getPersonalIdentityNumber()).isEqualTo("19800101-1111");
    }
}

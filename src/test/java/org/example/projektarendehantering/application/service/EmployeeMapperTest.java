package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeEntity;
import org.example.projektarendehantering.presentation.dto.EmployeeCreateDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeUpdateDTO;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeMapperTest {

    private final EmployeeMapper employeeMapper = new EmployeeMapper();

    @Test
    void toDTO_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        EmployeeEntity entity = EmployeeEntity.builder()
                .id(id)
                .displayName("Alice")
                .githubUsername("alice123")
                .role(Role.DOCTOR)
                .createdAt(now)
                .build();

        EmployeeDTO dto = employeeMapper.toDTO(entity);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getDisplayName()).isEqualTo("Alice");
        assertThat(dto.getGithubUsername()).isEqualTo("alice123");
        assertThat(dto.getRole()).isEqualTo(Role.DOCTOR);
        assertThat(dto.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_shouldMapAllFields() {
        EmployeeCreateDTO dto = EmployeeCreateDTO.builder()
                .displayName("Bob")
                .githubUsername("bob456")
                .role(Role.MANAGER)
                .build();

        EmployeeEntity entity = employeeMapper.toEntity(dto);

        assertThat(entity.getDisplayName()).isEqualTo("Bob");
        assertThat(entity.getGithubUsername()).isEqualTo("bob456");
        assertThat(entity.getRole()).isEqualTo(Role.MANAGER);
        assertThat(entity.getId()).isNotNull();
    }

    @Test
    void updateEntity_shouldUpdateAllFields() {
        EmployeeEntity entity = EmployeeEntity.builder()
                .displayName("OldName")
                .githubUsername("olduser")
                .role(Role.DOCTOR)
                .build();

        EmployeeUpdateDTO dto = EmployeeUpdateDTO.builder()
                .displayName("NewName")
                .githubUsername("newuser")
                .role(Role.MANAGER)
                .build();

        employeeMapper.updateEntity(dto, entity);

        assertThat(entity.getDisplayName()).isEqualTo("NewName");
        assertThat(entity.getGithubUsername()).isEqualTo("newuser");
        assertThat(entity.getRole()).isEqualTo(Role.MANAGER);
    }
}

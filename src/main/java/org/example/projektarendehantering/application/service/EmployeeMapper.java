package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.EmployeeEntity;
import org.example.projektarendehantering.presentation.dto.EmployeeCreateDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeUpdateDTO;
import org.springframework.stereotype.Component;
import org.example.projektarendehantering.common.GithubUsernameNormalizer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class EmployeeMapper {

    public EmployeeDTO toDTO(EmployeeEntity entity) {
        if (entity == null) return null;
        return EmployeeDTO.builder()
                .id(entity.getId())
                .displayName(entity.getDisplayName())
                .githubUsername(entity.getGithubUsername())
                .role(entity.getRole())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public EmployeeEntity toEntity(EmployeeCreateDTO dto) {
        if (dto == null) return null;
        String normalizedGithubUsername = GithubUsernameNormalizer.normalize(dto.getGithubUsername());
        UUID id = null;
        if (normalizedGithubUsername != null) {
            id = UUID.nameUUIDFromBytes(normalizedGithubUsername.getBytes(StandardCharsets.UTF_8));
        }

        return EmployeeEntity.builder()
                .id(id)
                .displayName(dto.getDisplayName())
                .githubUsername(normalizedGithubUsername)
                .role(dto.getRole())
                .build();
    }

    public void updateEntity(EmployeeUpdateDTO dto, EmployeeEntity entity) {
        if (dto == null || entity == null) return;
        entity.setDisplayName(dto.getDisplayName());
        entity.setGithubUsername(GithubUsernameNormalizer.normalize(dto.getGithubUsername()));
        entity.setRole(dto.getRole());
    }
}


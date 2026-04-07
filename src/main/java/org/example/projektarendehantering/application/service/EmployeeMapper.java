package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.EmployeeEntity;
import org.example.projektarendehantering.presentation.dto.EmployeeCreateDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeDTO;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class EmployeeMapper {

    public EmployeeDTO toDTO(EmployeeEntity entity) {
        if (entity == null) return null;
        return new EmployeeDTO(
                entity.getId(),
                entity.getDisplayName(),
                entity.getGithubUsername(),
                entity.getRole(),
                entity.getCreatedAt()
        );
    }

    public EmployeeEntity toEntity(EmployeeCreateDTO dto) {
        if (dto == null) return null;
        EmployeeEntity entity = new EmployeeEntity();
        entity.setDisplayName(dto.getDisplayName());
        entity.setGithubUsername(dto.getGithubUsername());
        entity.setRole(dto.getRole());

        if (dto.getGithubUsername() != null && !dto.getGithubUsername().isBlank()) {
            UUID id = UUID.nameUUIDFromBytes(dto.getGithubUsername().getBytes(StandardCharsets.UTF_8));
            entity.setId(id);
        }
        return entity;
    }
}


package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.EmployeeEntity;
import org.example.projektarendehantering.presentation.dto.EmployeeCreateDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeDTO;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeDTO toDTO(EmployeeEntity entity) {
        if (entity == null) return null;
        return new EmployeeDTO(
                entity.getId(),
                entity.getDisplayName(),
                entity.getRole(),
                entity.getCreatedAt()
        );
    }

    public EmployeeEntity toEntity(EmployeeCreateDTO dto) {
        if (dto == null) return null;
        EmployeeEntity entity = new EmployeeEntity();
        entity.setDisplayName(dto.getDisplayName());
        entity.setRole(dto.getRole());
        return entity;
    }
}


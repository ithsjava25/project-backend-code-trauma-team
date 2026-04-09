package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.PatientEntity;
import org.example.projektarendehantering.presentation.dto.PatientCreateDTO;
import org.example.projektarendehantering.presentation.dto.PatientDTO;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public PatientDTO toDTO(PatientEntity entity) {
        if (entity == null) return null;
        return new PatientDTO(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPersonalIdentityNumber(),
                entity.getCreatedAt()
        );
    }

    public PatientEntity toEntity(PatientCreateDTO dto) {
        if (dto == null) return null;
        PatientEntity entity = new PatientEntity();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setPersonalIdentityNumber(dto.getPersonalIdentityNumber());
        return entity;
    }
}

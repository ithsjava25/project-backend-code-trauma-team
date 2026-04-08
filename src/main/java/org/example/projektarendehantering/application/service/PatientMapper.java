package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.PatientProfileEntity;
import org.example.projektarendehantering.presentation.dto.PatientCreateDTO;
import org.example.projektarendehantering.presentation.dto.PatientDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PatientMapper {

    public PatientDTO toDTO(PatientProfileEntity entity) {
        if (entity == null) return null;
        Instant createdAt = entity.getAccount() != null ? entity.getAccount().getCreatedAt() : null;
        return new PatientDTO(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                createdAt
        );
    }

    public PatientProfileEntity toEntity(PatientCreateDTO dto) {
        if (dto == null) return null;
        PatientProfileEntity entity = new PatientProfileEntity();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setPersonalIdentityNumber(dto.getPersonalIdentityNumber());
        return entity;
    }
}

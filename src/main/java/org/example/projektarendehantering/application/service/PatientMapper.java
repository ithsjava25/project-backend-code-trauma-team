package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.PatientEntity;
import org.example.projektarendehantering.presentation.dto.PatientCreateDTO;
import org.example.projektarendehantering.presentation.dto.PatientDTO;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public PatientDTO toDTO(PatientEntity entity) {
        if (entity == null) return null;
        return PatientDTO.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .personalIdentityNumber(entity.getPersonalIdentityNumber())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public PatientEntity toEntity(PatientCreateDTO dto) {
        if (dto == null) return null;
        return PatientEntity.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .personalIdentityNumber(dto.getPersonalIdentityNumber())
                .build();
    }
}

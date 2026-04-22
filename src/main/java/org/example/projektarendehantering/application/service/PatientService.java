package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.BadRequestException;
import org.example.projektarendehantering.common.ConflictException;
import org.example.projektarendehantering.infrastructure.persistence.PatientEntity;
import org.example.projektarendehantering.infrastructure.persistence.PatientRepository;
import org.example.projektarendehantering.presentation.dto.PatientCreateDTO;
import org.example.projektarendehantering.presentation.dto.PatientDTO;
import org.example.projektarendehantering.presentation.dto.PatientUpdateDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @Transactional
    public PatientDTO createPatient(PatientCreateDTO patientDTO) {
        PatientEntity entity = patientMapper.toEntity(patientDTO);
        entity.setId(UUID.randomUUID());
        entity.setCreatedAt(Instant.now());
        if (entity.getPersonalIdentityNumber() != null && !entity.getPersonalIdentityNumber().isBlank()) {
            patientRepository.findByPersonalIdentityNumber(entity.getPersonalIdentityNumber())
                    .ifPresent(existing -> {
                        throw new ConflictException("Patient with personalIdentityNumber already exists");
                    });
        }
        return patientMapper.toDTO(patientRepository.save(entity));
    }

    @Transactional
    public PatientDTO updatePatient(UUID id, PatientUpdateDTO dto) {
        PatientEntity entity = patientRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("PATIENT_NOT_FOUND", "Patient not found"));

        if (!entity.getPersonalIdentityNumber().equals(dto.getPersonalIdentityNumber())) {
            patientRepository.findByPersonalIdentityNumber(dto.getPersonalIdentityNumber())
                    .ifPresent(existing -> {
                        throw new ConflictException("Patient with personalIdentityNumber already exists");
                    });
        }

        patientMapper.updateEntity(dto, entity);
        return patientMapper.toDTO(patientRepository.save(entity));
    }

    @Transactional
    public void deletePatient(UUID id) {
        PatientEntity entity = patientRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("PATIENT_NOT_FOUND", "Patient not found"));
        patientRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public Optional<PatientDTO> getPatient(UUID id) {
        return patientRepository.findById(id).map(patientMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toDTO)
                .collect(Collectors.toList());
    }
}

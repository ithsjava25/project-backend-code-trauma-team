package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.PatientEntity;
import org.example.projektarendehantering.infrastructure.persistence.PatientRepository;
import org.example.projektarendehantering.presentation.dto.PatientCreateDTO;
import org.example.projektarendehantering.presentation.dto.PatientDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientService(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    @Transactional
    public PatientDTO createPatient(PatientCreateDTO patientDTO) {
        PatientEntity entity = patientMapper.toEntity(patientDTO);
        // Removed manual setId - @GeneratedValue in PatientEntity will handle this
        entity.setCreatedAt(Instant.now());
        if (entity.getPersonalIdentityNumber() != null && !entity.getPersonalIdentityNumber().isBlank()) {
            patientRepository.findByPersonalIdentityNumber(entity.getPersonalIdentityNumber())
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient with personalIdentityNumber already exists");
                    });
        }
        return patientMapper.toDTO(patientRepository.save(entity));
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

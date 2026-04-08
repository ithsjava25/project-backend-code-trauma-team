package org.example.projektarendehantering.identity.application;

import org.example.projektarendehantering.shared.Role;
import org.example.projektarendehantering.identity.domain.AccountEntity;
import org.example.projektarendehantering.identity.domain.AccountRepository;
import org.example.projektarendehantering.identity.domain.PatientProfileEntity;
import org.example.projektarendehantering.identity.domain.PatientProfileRepository;
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

    private final AccountRepository accountRepository;
    private final PatientProfileRepository patientProfileRepository;

    public PatientService(AccountRepository accountRepository, PatientProfileRepository patientProfileRepository) {
        this.accountRepository = accountRepository;
        this.patientProfileRepository = patientProfileRepository;
    }

    @Transactional
    public PatientDTO createPatient(PatientCreateDTO dto) {
        if (dto.getPersonalIdentityNumber() != null && !dto.getPersonalIdentityNumber().isBlank()) {
            patientProfileRepository.findByPersonalIdentityNumber(dto.getPersonalIdentityNumber())
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient with personalIdentityNumber already exists");
                    });
        }

        UUID id = UUID.randomUUID();
        String displayName = (dto.getFirstName() + " " + dto.getLastName()).trim();
        
        // 1. Create central account (Manual Patients don't have GitHub login)
        AccountEntity account = new AccountEntity(id, null, displayName, Role.PATIENT, Instant.now());
        accountRepository.save(account);

        // 2. Create profile
        PatientProfileEntity profile = new PatientProfileEntity(account, dto.getFirstName(), dto.getLastName(), dto.getPersonalIdentityNumber());
        patientProfileRepository.save(profile);

        return new PatientDTO(profile.getId(), profile.getFirstName(), profile.getLastName(), account.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public Optional<PatientDTO> getPatient(UUID id) {
        return patientProfileRepository.findById(id).map(p -> new PatientDTO(p.getId(), p.getFirstName(), p.getLastName(), p.getAccount().getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        return patientProfileRepository.findAll().stream()
                .map(p -> new PatientDTO(p.getId(), p.getFirstName(), p.getLastName(), p.getAccount().getCreatedAt()))
                .collect(Collectors.toList());
    }
}

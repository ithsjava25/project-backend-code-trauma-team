package org.example.projektarendehantering.application.service;

import lombok.RequiredArgsConstructor;
import org.example.projektarendehantering.common.BadRequestException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.AuthProvider;
import org.example.projektarendehantering.infrastructure.persistence.PatientEntity;
import org.example.projektarendehantering.infrastructure.persistence.PatientRepository;
import org.example.projektarendehantering.infrastructure.persistence.UserAccountEntity;
import org.example.projektarendehantering.infrastructure.persistence.UserAccountRepository;
import org.example.projektarendehantering.presentation.dto.PatientRegistrationDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserAccountRepository userAccountRepository;
    private final PatientRepository patientRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerPatient(PatientRegistrationDTO registrationDTO) {

        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new BadRequestException("PASSWORDS_DO_NOT_MATCH", "Passwords do not match");
        }

        var normalizedEmail = registrationDTO.getEmail().trim().toLowerCase(Locale.ROOT);
        var firstName = registrationDTO.getFirstName().trim();
        var lastName = registrationDTO.getLastName().trim();
        var personalIdentityNumber = registrationDTO.getPersonalIdentityNumber().trim();

        userAccountRepository.findByEmail(normalizedEmail).ifPresent(user -> {
            throw new BadRequestException("USER_EXISTS", "A user with this email already exists");
        });
        patientRepository.findByPersonalIdentityNumber(personalIdentityNumber).ifPresent(existing -> {
            throw new BadRequestException("PATIENT_EXISTS", "A patient with this personal identity number already exists");
        });

        var newUserAccount = new UserAccountEntity();
        newUserAccount.setEmail(normalizedEmail);
        newUserAccount.setPasswordHash(passwordEncoder.encode(registrationDTO.getPassword()));
        newUserAccount.setRole(Role.PATIENT);
        newUserAccount.setProvider(AuthProvider.LOCAL);
        newUserAccount.setEnabled(true);

        UserAccountEntity savedAccount;
        try {
            savedAccount = userAccountRepository.save(newUserAccount);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("USER_EXISTS", "A user with this email already exists");
        }

        PatientEntity patientEntity = new PatientEntity();
        patientEntity.setId(savedAccount.getId());
        patientEntity.setFirstName(firstName);
        patientEntity.setLastName(lastName);
        patientEntity.setPersonalIdentityNumber(personalIdentityNumber);
        patientEntity.setCreatedAt(savedAccount.getCreatedAt());

        try {
            patientRepository.save(patientEntity);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("PATIENT_EXISTS", "A patient with this personal identity number already exists");
        }
    }
}

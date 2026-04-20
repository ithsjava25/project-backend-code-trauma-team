package org.example.projektarendehantering.application.service;

import lombok.RequiredArgsConstructor;
import org.example.projektarendehantering.common.BadRequestException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.AuthProvider;
import org.example.projektarendehantering.infrastructure.persistence.UserAccountEntity;
import org.example.projektarendehantering.infrastructure.persistence.UserAccountRepository;
import org.example.projektarendehantering.presentation.dto.PatientRegistrationDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserAccountRepository userAccountRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerPatient(PatientRegistrationDTO registrationDTO) {

        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new BadRequestException("PASSWORDS_DO_NOT_MATCH", "Passwords do not match");
        }

        var normalizedEmail = registrationDTO.getEmail().trim().toLowerCase();

        userAccountRepository.findByEmail(normalizedEmail).ifPresent(user -> {
            throw new BadRequestException("USER_EXISTS", "A user with this email already exists");
        });

        var newUserAccount = new UserAccountEntity();
        newUserAccount.setEmail(normalizedEmail);
        newUserAccount.setPasswordHash(passwordEncoder.encode(registrationDTO.getPassword()));
        newUserAccount.setRole(Role.PATIENT);
        newUserAccount.setProvider(AuthProvider.LOCAL);
        newUserAccount.setEnabled(true);

        try {
            userAccountRepository.save(newUserAccount);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("USER_EXISTS", "A user with this email already exists");
        }
    }
}

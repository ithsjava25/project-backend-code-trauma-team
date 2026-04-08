package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.BadRequestException;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.*;
import org.example.projektarendehantering.presentation.dto.EmployeeCreateDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final AccountRepository accountRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final PatientProfileRepository patientProfileRepository;

    public EmployeeService(AccountRepository accountRepository, 
                           EmployeeProfileRepository employeeProfileRepository,
                           PatientProfileRepository patientProfileRepository) {
        this.accountRepository = accountRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.patientProfileRepository = patientProfileRepository;
    }

    @Transactional
    public EmployeeDTO createEmployee(Actor actor, EmployeeCreateDTO dto) {
        requireCanManageEmployees(actor);

        UUID userId = UUID.nameUUIDFromBytes(dto.getGithubUsername().getBytes(StandardCharsets.UTF_8));
        
        // 1. Create or Update Account
        AccountEntity account = accountRepository.findById(userId).orElseGet(() -> {
            AccountEntity newAccount = new AccountEntity();
            newAccount.setId(userId);
            newAccount.setGithubUsername(dto.getGithubUsername());
            newAccount.setCreatedAt(Instant.now());
            return newAccount;
        });

        account.setDisplayName(dto.getDisplayName());
        account.setRole(dto.getRole());
        
        // Gaining a managed instance from the repository
        AccountEntity savedAccount = accountRepository.save(account);

        // 2. Create corresponding Profile
        if (dto.getRole() == Role.PATIENT) {
            if (!patientProfileRepository.existsById(userId)) {
                PatientProfileEntity profile = new PatientProfileEntity(savedAccount, dto.getDisplayName(), "", "");
                patientProfileRepository.save(profile);
            }
        } else if (dto.getRole() != Role.PENDING) {
            if (!employeeProfileRepository.existsById(userId)) {
                EmployeeProfileEntity profile = new EmployeeProfileEntity(savedAccount);
                employeeProfileRepository.save(profile);
            }
        }

        return new EmployeeDTO(savedAccount.getId(), savedAccount.getDisplayName(), savedAccount.getGithubUsername(), savedAccount.getRole(), savedAccount.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeDTO> getEmployee(Actor actor, UUID id) {
        requireCanManageEmployees(actor);
        return accountRepository.findById(id).map(a -> new EmployeeDTO(a.getId(), a.getDisplayName(), a.getGithubUsername(), a.getRole(), a.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllEmployees(Actor actor) {
        requireCanManageEmployees(actor);
        return accountRepository.findAll().stream()
                .map(a -> new EmployeeDTO(a.getId(), a.getDisplayName(), a.getGithubUsername(), a.getRole(), a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    private void requireCanManageEmployees(Actor actor) {
        if (actor == null) throw new NotAuthorizedException("Missing actor");
        if (actor.role() == Role.MANAGER) return;
        throw new NotAuthorizedException("Not allowed to access employees");
    }
}

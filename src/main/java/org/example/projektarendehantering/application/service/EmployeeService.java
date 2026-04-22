package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.BadRequestException;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeEntity;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeRepository;
import org.example.projektarendehantering.presentation.dto.EmployeeCreateDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeUpdateDTO;
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
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Transactional
    public EmployeeDTO createEmployee(Actor actor, EmployeeCreateDTO dto) {
        requireCanManageEmployees(actor);

        if (employeeRepository.findByGithubUsername(dto.getGithubUsername()).isPresent()) {
            throw new BadRequestException("EMPLOYEE_EXISTS", "Employee with username " + dto.getGithubUsername() + " already exists");
        }

        EmployeeEntity entity = employeeMapper.toEntity(dto);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        entity.setCreatedAt(Instant.now());
        return employeeMapper.toDTO(employeeRepository.save(entity));
    }

    @Transactional
    public EmployeeDTO updateEmployee(Actor actor, UUID id, EmployeeUpdateDTO dto) {
        requireCanManageEmployees(actor);
        EmployeeEntity entity = employeeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("EMPLOYEE_NOT_FOUND", "Employee not found"));

        if (!entity.getGithubUsername().equals(dto.getGithubUsername())) {
            throw new BadRequestException("EMPLOYEE_USERNAME_IMMUTABLE", "Github username cannot be changed");
        }

        employeeMapper.updateEntity(dto, entity);
        return employeeMapper.toDTO(employeeRepository.save(entity));
    }

    @Transactional
    public void deleteEmployee(Actor actor, UUID id) {
        requireCanManageEmployees(actor);
        EmployeeEntity entity = employeeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("EMPLOYEE_NOT_FOUND", "Employee not found"));
        employeeRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeDTO> getEmployee(Actor actor, UUID id) {
        requireCanManageEmployees(actor);
        return employeeRepository.findById(id).map(employeeMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllEmployees(Actor actor) {
        requireCanManageEmployees(actor);
        return employeeRepository.findAll().stream()
                .map(employeeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> findByRole(Role role) {
        return employeeRepository.findAllByRole(role).stream()
                .map(employeeMapper::toDTO)
                .collect(Collectors.toList());
    }

    private void requireCanManageEmployees(Actor actor) {
        if (actor == null) {
            throw new NotAuthorizedException("Missing actor");
        }
        if (actor.role() == Role.MANAGER) {
            return;
        }
        throw new NotAuthorizedException("Not allowed to access employees");
    }
}


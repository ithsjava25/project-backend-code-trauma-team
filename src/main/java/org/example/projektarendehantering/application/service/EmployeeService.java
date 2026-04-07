package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeEntity;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeRepository;
import org.example.projektarendehantering.presentation.dto.EmployeeCreateDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }

    @Transactional
    public EmployeeDTO createEmployee(Actor actor, EmployeeCreateDTO dto) {
        requireCanManageEmployees(actor);
        EmployeeEntity entity = employeeMapper.toEntity(dto);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        entity.setCreatedAt(Instant.now());
        return employeeMapper.toDTO(employeeRepository.save(entity));
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


package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeEntity;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeRepository;
import org.example.projektarendehantering.presentation.dto.EmployeeCreateDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Actor managerActor;
    private Actor doctorActor;

    @BeforeEach
    void setUp() {
        managerActor = new Actor(UUID.randomUUID(), Role.MANAGER, "Manager", "manager_user");
        doctorActor = new Actor(UUID.randomUUID(), Role.DOCTOR, "Doctor", "doctor_user");
    }

    @Test
    void getAllEmployees_shouldAllowManager() {
        when(employeeRepository.findAll()).thenReturn(List.of(new EmployeeEntity()));
        when(employeeMapper.toDTO(any())).thenReturn(new EmployeeDTO(UUID.randomUUID(), "Name", "gh_user", Role.DOCTOR, Instant.now()));

        List<EmployeeDTO> result = employeeService.getAllEmployees(managerActor);

        assertThat(result).isNotEmpty();
        verify(employeeRepository).findAll();
    }

    @Test
    void getAllEmployees_shouldDenyDoctor() {
        assertThatThrownBy(() -> employeeService.getAllEmployees(doctorActor))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessageContaining("Not allowed to access employees");
    }

    @Test
    void createEmployee_shouldAllowManager() {
        EmployeeCreateDTO dto = new EmployeeCreateDTO();
        EmployeeEntity entity = new EmployeeEntity();
        EmployeeDTO resultDTO = new EmployeeDTO(UUID.randomUUID(), "Name", "gh_user", Role.DOCTOR, Instant.now());

        when(employeeMapper.toEntity(dto)).thenReturn(entity);
        when(employeeRepository.save(any())).thenReturn(entity);
        when(employeeMapper.toDTO(entity)).thenReturn(resultDTO);

        EmployeeDTO result = employeeService.createEmployee(managerActor, dto);

        assertThat(result).isNotNull();
        verify(employeeRepository).save(any());
    }

    @Test
    void createEmployee_shouldDenyDoctor() {
        EmployeeCreateDTO dto = new EmployeeCreateDTO();
        assertThatThrownBy(() -> employeeService.createEmployee(doctorActor, dto))
                .isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    void requireCanManageEmployees_shouldThrow_whenActorIsNull() {
        assertThatThrownBy(() -> employeeService.getAllEmployees(null))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessageContaining("Missing actor");
    }
}

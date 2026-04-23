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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
        EmployeeCreateDTO dto = new EmployeeCreateDTO("Name", " Gh_User ", Role.DOCTOR);
        EmployeeEntity entity = new EmployeeEntity();
        EmployeeDTO resultDTO = new EmployeeDTO(UUID.randomUUID(), "Name", "gh_user", Role.DOCTOR, Instant.now());

        when(employeeRepository.findByGithubUsername("gh_user")).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(dto)).thenReturn(entity);
        when(employeeRepository.save(any())).thenReturn(entity);
        when(employeeMapper.toDTO(entity)).thenReturn(resultDTO);

        EmployeeDTO result = employeeService.createEmployee(managerActor, dto);

        assertThat(result).isNotNull();
        assertThat(dto.getGithubUsername()).isEqualTo("gh_user");
        verify(employeeRepository).save(any());
    }

    @Test
    void createEmployee_shouldDenyDoctor() {
        EmployeeCreateDTO dto = new EmployeeCreateDTO();
        assertThatThrownBy(() -> employeeService.createEmployee(doctorActor, dto))
                .isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    void updateEmployee_shouldUpdateIfManager() {
        UUID id = UUID.randomUUID();
        EmployeeUpdateDTO dto = new EmployeeUpdateDTO("New Name", "gh_user", Role.DOCTOR);
        EmployeeEntity entity = new EmployeeEntity();
        entity.setGithubUsername("gh_user");
        EmployeeDTO resultDTO = new EmployeeDTO(id, "New Name", "gh_user", Role.DOCTOR, Instant.now());

        when(employeeRepository.findById(id)).thenReturn(Optional.of(entity));
        when(employeeRepository.save(entity)).thenReturn(entity);
        when(employeeMapper.toDTO(entity)).thenReturn(resultDTO);

        EmployeeDTO result = employeeService.updateEmployee(managerActor, id, dto);

        assertThat(result.getDisplayName()).isEqualTo("New Name");
        verify(employeeMapper).updateEntity(dto, entity);
    }

    @Test
    void updateEmployee_shouldThrowIfUsernameChanged() {
        UUID id = UUID.randomUUID();
        EmployeeUpdateDTO dto = new EmployeeUpdateDTO("New Name", "new_gh", Role.DOCTOR);
        EmployeeEntity entity = new EmployeeEntity();
        entity.setGithubUsername("old_gh");

        when(employeeRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> employeeService.updateEmployee(managerActor, id, dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Github username cannot be changed");
    }

    @Test
    void updateEmployee_shouldAllowEquivalentUsernameAfterNormalization() {
        UUID id = UUID.randomUUID();
        EmployeeUpdateDTO dto = new EmployeeUpdateDTO("New Name", " GH_USER ", Role.DOCTOR);
        EmployeeEntity entity = new EmployeeEntity();
        entity.setGithubUsername("gh_user");
        EmployeeDTO resultDTO = new EmployeeDTO(id, "New Name", "gh_user", Role.DOCTOR, Instant.now());

        when(employeeRepository.findById(id)).thenReturn(Optional.of(entity));
        when(employeeRepository.save(entity)).thenReturn(entity);
        when(employeeMapper.toDTO(entity)).thenReturn(resultDTO);

        EmployeeDTO result = employeeService.updateEmployee(managerActor, id, dto);

        assertThat(result).isNotNull();
        assertThat(dto.getGithubUsername()).isEqualTo("gh_user");
        verify(employeeMapper).updateEntity(dto, entity);
    }

    @Test
    void deleteEmployee_shouldDeleteIfManager() {
        UUID id = UUID.randomUUID();
        EmployeeEntity entity = new EmployeeEntity();

        when(employeeRepository.findById(id)).thenReturn(Optional.of(entity));

        employeeService.deleteEmployee(managerActor, id);

        verify(employeeRepository).delete(entity);
    }

    @Test
    void requireCanManageEmployees_shouldThrow_whenActorIsNull() {
        assertThatThrownBy(() -> employeeService.getAllEmployees(null))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessageContaining("Missing actor");
    }
}

package org.example.projektarendehantering.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.projektarendehantering.application.service.EmployeeService;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.example.projektarendehantering.presentation.dto.EmployeeCreateDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class EmployeeControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private SecurityActorAdapter securityActorAdapter;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Actor managerActor;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        managerActor = new Actor(UUID.randomUUID(), Role.MANAGER);
        when(securityActorAdapter.currentUser()).thenReturn(managerActor);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getAllEmployees_shouldReturnList() throws Exception {
        EmployeeDTO dto = new EmployeeDTO(UUID.randomUUID(), "Manager Name", Role.MANAGER, null);

        when(employeeService.getAllEmployees(managerActor)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].displayName").value("Manager Name"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createEmployee_shouldReturnCreatedEmployee() throws Exception {
        EmployeeCreateDTO input = new EmployeeCreateDTO();
        input.setDisplayName("New Employee");
        input.setRole(Role.DOCTOR);

        EmployeeDTO output = new EmployeeDTO(UUID.randomUUID(), "New Employee", Role.DOCTOR, null);

        when(employeeService.createEmployee(eq(managerActor), any(EmployeeCreateDTO.class))).thenReturn(output);

        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("New Employee"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getEmployee_shouldReturnEmployee_whenExists() throws Exception {
        UUID empId = UUID.randomUUID();
        EmployeeDTO dto = new EmployeeDTO(empId, "Some Name", Role.NURSE, null);

        when(employeeService.getEmployee(eq(managerActor), eq(empId))).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/employees/{id}", empId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(empId.toString()));
    }
}

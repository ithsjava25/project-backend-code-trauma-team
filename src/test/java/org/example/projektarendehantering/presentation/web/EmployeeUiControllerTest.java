package org.example.projektarendehantering.presentation.web;

import org.example.projektarendehantering.application.service.EmployeeService;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.example.projektarendehantering.presentation.dto.EmployeeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class EmployeeUiControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private SecurityActorAdapter securityActorAdapter;

    private Actor managerActor;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        managerActor = new Actor(UUID.randomUUID(), Role.MANAGER, "Manager", "manager_user");
        when(securityActorAdapter.currentUser()).thenReturn(managerActor);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void listEmployees_shouldReturnView() throws Exception {
        mockMvc.perform(get("/ui/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"))
                .andExpect(model().attributeExists("employees"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void editEmployee_shouldReturnView() throws Exception {
        UUID id = UUID.randomUUID();
        EmployeeDTO employee = new EmployeeDTO(id, "Name", "gh", Role.DOCTOR, Instant.now());
        when(employeeService.getEmployee(eq(managerActor), eq(id))).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/ui/employees/edit/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/edit"))
                .andExpect(model().attributeExists("employeeUpdateDTO"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void deleteEmployee_shouldRedirect() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/ui/employees/delete/{id}", id).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/employees"));
    }
}

package org.example.projektarendehantering.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.projektarendehantering.application.service.PatientService;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.example.projektarendehantering.presentation.dto.PatientCreateDTO;
import org.example.projektarendehantering.presentation.dto.PatientDTO;
import org.example.projektarendehantering.presentation.dto.PatientUpdateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class PatientControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private SecurityActorAdapter securityActorAdapter;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Actor managerActor;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        managerActor = new Actor(UUID.randomUUID(), Role.MANAGER, "Manager", "mgr");
        when(securityActorAdapter.currentUser()).thenReturn(managerActor);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createPatient_shouldReturnOk() throws Exception {
        PatientCreateDTO input = new PatientCreateDTO("John", "Doe", "19900101-1234");
        PatientDTO output = new PatientDTO();
        output.setFirstName("John");

        when(patientService.createPatient(any(PatientCreateDTO.class))).thenReturn(output);

        mockMvc.perform(post("/api/patients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updatePatient_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        PatientUpdateDTO input = new PatientUpdateDTO("Jane", "Doe", "19900101-1234");
        PatientDTO output = new PatientDTO();
        output.setFirstName("Jane");

        when(patientService.updatePatient(eq(managerActor), eq(id), any(PatientUpdateDTO.class))).thenReturn(output);

        mockMvc.perform(put("/api/patients/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void deletePatient_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/patients/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(patientService).deletePatient(managerActor, id);
    }
}

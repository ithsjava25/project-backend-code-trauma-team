package org.example.projektarendehantering.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.projektarendehantering.application.service.CaseService;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
class CaseControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private CaseService caseService;

    @MockitoBean
    private SecurityActorAdapter securityActorAdapter;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Actor doctorActor;
    private UUID caseId;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules(); // Ensure Java 8 time etc are supported if needed
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        caseId = UUID.randomUUID();
        doctorActor = new Actor(UUID.randomUUID(), Role.DOCTOR);
        when(securityActorAdapter.currentUser()).thenReturn(doctorActor);
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void getAllCases_shouldReturnList() throws Exception {
        CaseDTO caseDTO = new CaseDTO();
        caseDTO.setId(caseId);
        caseDTO.setDescription("Test Case");

        when(caseService.getAllCases(doctorActor)).thenReturn(List.of(caseDTO));

        mockMvc.perform(get("/api/cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(caseId.toString()))
                .andExpect(jsonPath("$[0].description").value("Test Case"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void getCase_shouldReturnCase_whenExists() throws Exception {
        CaseDTO caseDTO = new CaseDTO();
        caseDTO.setId(caseId);

        when(caseService.getCase(eq(doctorActor), eq(caseId))).thenReturn(Optional.of(caseDTO));

        mockMvc.perform(get("/api/cases/{id}", caseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(caseId.toString()));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void getCase_shouldReturnNotFound_whenDoesNotExist() throws Exception {
        when(caseService.getCase(eq(doctorActor), eq(caseId))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cases/{id}", caseId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void createCase_shouldReturnCreatedCase() throws Exception {
        CaseDTO inputDTO = new CaseDTO();
        inputDTO.setDescription("New Case");
        inputDTO.setPatientId(UUID.randomUUID());

        CaseDTO outputDTO = new CaseDTO();
        outputDTO.setId(UUID.randomUUID());
        outputDTO.setDescription("New Case");

        when(caseService.createCase(eq(doctorActor), any(CaseDTO.class))).thenReturn(outputDTO);

        mockMvc.perform(post("/api/cases")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("New Case"));
    }

    @Test
    void getAllCases_shouldReturnUnauthorized_whenNotLoggedIn() throws Exception {
        // Without @WithMockUser
        mockMvc.perform(get("/api/cases"))
                .andExpect(status().is3xxRedirection()); // Redirect to login in OAuth2 setup
    }
}

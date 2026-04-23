package org.example.projektarendehantering.presentation.web;

import org.example.projektarendehantering.application.service.CaseService;
import org.example.projektarendehantering.application.service.PatientService;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class UiControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private CaseService caseService;

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private SecurityActorAdapter securityActorAdapter;

    private Actor doctorActor;
    private UUID caseId;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        caseId = UUID.randomUUID();
        doctorActor = new Actor(UUID.randomUUID(), Role.DOCTOR, "Doctor", "doctor_user");
        when(securityActorAdapter.currentUser()).thenReturn(doctorActor);
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void caseDetail_shouldReturnErrorPage_whenNotAuthorized() throws Exception {
        when(caseService.getCase(eq(doctorActor), eq(caseId)))
                .thenThrow(new NotAuthorizedException("NOT_ALLOWED", "Not allowed to read this case"));

        mockMvc.perform(get("/ui/cases/{caseId}", caseId))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("status", "403 Forbidden"))
                .andExpect(model().attribute("message", "Not allowed to read this case"))
                .andExpect(model().attribute("errorCode", "NOT_ALLOWED"));
    }
}

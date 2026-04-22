package org.example.projektarendehantering.presentation.web;

import org.example.projektarendehantering.application.service.PatientService;
import org.example.projektarendehantering.presentation.dto.PatientDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class PatientUiControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PatientService patientService;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void listPatients_shouldReturnView() throws Exception {
        mockMvc.perform(get("/ui/patients"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/list"))
                .andExpect(model().attributeExists("patients"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void editPatient_shouldReturnView() throws Exception {
        UUID id = UUID.randomUUID();
        PatientDTO patient = new PatientDTO();
        patient.setFirstName("John");
        when(patientService.getPatient(id)).thenReturn(Optional.of(patient));

        mockMvc.perform(get("/ui/patients/edit/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/edit"))
                .andExpect(model().attributeExists("patientUpdateDTO"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void deletePatient_shouldRedirect() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/ui/patients/delete/{id}", id).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/patients"));
    }
}

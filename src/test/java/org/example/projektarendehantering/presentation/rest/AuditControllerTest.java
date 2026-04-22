package org.example.projektarendehantering.presentation.rest;

import org.example.projektarendehantering.application.service.AuditService;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.example.projektarendehantering.presentation.dto.AuditEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class AuditControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private SecurityActorAdapter securityActorAdapter;

    private Actor managerActor;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        managerActor = new Actor(UUID.randomUUID(), Role.MANAGER, "Manager", "manager");
        when(securityActorAdapter.currentUser()).thenReturn(managerActor);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void list_shouldReturnAuditEvents() throws Exception {
        AuditEventDTO dto = new AuditEventDTO();
        dto.setRequestPath("/api/cases");
        
        Page<AuditEventDTO> page = new PageImpl<>(List.of(dto));
        
        when(auditService.listEvents(eq(managerActor), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].requestPath").value("/api/cases"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void list_withFilters_shouldPassParameters() throws Exception {
        UUID caseId = UUID.randomUUID();
        
        when(auditService.listEvents(eq(managerActor), any(), any(), eq(caseId), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/audit")
                        .param("caseId", caseId.toString())
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(auditService).listEvents(
                eq(managerActor),
                isNull(),
                isNull(),
                eq(caseId),
                argThat(p -> p.getPageNumber() == 1 && p.getPageSize() == 10)
        );
    }

    @Test
    void list_shouldBeUnauthorized_whenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/audit"))
                .andExpect(status().isUnauthorized());
    }
}

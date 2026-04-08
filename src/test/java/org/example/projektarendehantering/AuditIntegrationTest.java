package org.example.projektarendehantering;

import org.example.projektarendehantering.application.service.AuditService;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.AuditEventEntity;
import org.example.projektarendehantering.infrastructure.persistence.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@Transactional
class AuditIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private AuditService auditService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"MANAGER"})
    void anyRequest_shouldBeAudited() throws Exception {
        long countBefore = auditEventRepository.count();

        mockMvc.perform(get("/api/cases"))
                .andExpect(status().isOk());

        List<AuditEventEntity> events = auditEventRepository.findAll();
        assertThat(events.size()).isGreaterThan((int) countBefore);
        
        AuditEventEntity latest = events.get(events.size() - 1);
        assertThat(latest.getRequestPath()).isEqualTo("/api/cases");
        assertThat(latest.getHttpMethod()).isEqualTo("GET");
    }

    @Test
    void record_shouldSanitizeSensitiveQueryParameters() {
        AuditEventEntity event = new AuditEventEntity();
        event.setRequestPath("/api/login");
        event.setHttpMethod("POST");
        event.setQueryString("username=oscar&password=secretPassword123&token=abc-123");

        auditService.record(event);

        List<AuditEventEntity> events = auditEventRepository.findAll();
        AuditEventEntity saved = events.get(events.size() - 1);
        
        assertThat(saved.getQueryString())
                .contains("username=oscar")
                .contains("password=[REDACTED]")
                .contains("token=[REDACTED]");
    }

    @Test
    void record_shouldSanitizeSensitiveJsonPayload() {
        AuditEventEntity event = new AuditEventEntity();
        event.setRequestPath("/api/users");
        event.setHttpMethod("POST");
        event.setQueryString("{\"name\": \"Oscar\", \"secret\": \"top-secret\", \"ssn\": \"12345\"}");

        auditService.record(event);

        List<AuditEventEntity> events = auditEventRepository.findAll();
        AuditEventEntity saved = events.get(events.size() - 1);
        
        assertThat(saved.getQueryString())
                .contains("\"name\":\"Oscar\"")
                .contains("\"secret\":\"[REDACTED]\"")
                .contains("\"ssn\":\"[REDACTED]\"");
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void auditInterceptor_shouldCaptureCaseId_fromUri() throws Exception {
        UUID caseId = UUID.randomUUID();
        
        mockMvc.perform(get("/api/cases/{id}", caseId))
                .andExpect(status().isNotFound()); // Case doesn't exist, but that's fine for auditing

        List<AuditEventEntity> events = auditEventRepository.findAll();
        AuditEventEntity latest = events.get(events.size() - 1);
        
        assertThat(latest.getCaseId()).isEqualTo(caseId);
    }
}

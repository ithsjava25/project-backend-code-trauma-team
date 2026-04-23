package org.example.projektarendehantering;

import org.example.projektarendehantering.application.service.AuditService;
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

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void mutationRequest_shouldBeAudited() throws Exception {
        long countBefore = auditEventRepository.count();

        mockMvc.perform(post("/api/cases").with(csrf()))
                .andExpect(status().isBadRequest());

        List<AuditEventEntity> events = auditEventRepository.findAll();
        assertThat(events.size()).isGreaterThan((int) countBefore);

        AuditEventEntity latest = events.stream()
                .max(Comparator.comparing(AuditEventEntity::getOccurredAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(AuditEventEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow();
        assertThat(latest.getRequestPath()).isEqualTo("/api/cases");
    }

    @Test
    void record_shouldSanitizeSensitiveQueryParameters() {
        AuditEventEntity event = new AuditEventEntity();
        event.setRequestPath("/api/login");
        event.setQueryString("username=oscar&password=secretPassword123&token=abc-123");

        auditService.record(event);

        AuditEventEntity saved = auditEventRepository.findAll().stream()
                .max(Comparator.comparing(AuditEventEntity::getOccurredAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(AuditEventEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow();

        assertThat(saved.getQueryString())
                .contains("username=oscar")
                .contains("password=[REDACTED]")
                .contains("token=[REDACTED]");
    }

    @Test
    void record_shouldSanitizeSensitiveJsonPayload() {
        AuditEventEntity event = new AuditEventEntity();
        event.setRequestPath("/api/users");
        event.setQueryString("{\"name\": \"Oscar\", \"secret\": \"top-secret\", \"ssn\": \"12345\"}");

        auditService.record(event);

        AuditEventEntity saved = auditEventRepository.findAll().stream()
                .max(Comparator.comparing(AuditEventEntity::getOccurredAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(AuditEventEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow();

        assertThat(saved.getQueryString())
                .contains("\"name\":\"Oscar\"")
                .contains("\"secret\":\"[REDACTED]\"")
                .contains("\"ssn\":\"[REDACTED]\"");
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void auditInterceptor_shouldCaptureCaseId_fromMutationUri() throws Exception {
        UUID caseId = UUID.randomUUID();

        mockMvc.perform(delete("/api/cases/{id}", caseId).with(csrf()))
                .andExpect(status().isNotFound()); // Case doesn't exist, but that's fine for auditing

        AuditEventEntity latest = auditEventRepository.findAll().stream()
                .max(Comparator.comparing(AuditEventEntity::getOccurredAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(AuditEventEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow();

        assertThat(latest.getCaseId()).isEqualTo(caseId);
    }
}

package org.example.projektarendehantering;

import org.example.projektarendehantering.infrastructure.persistence.AuditEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class ProjektArendehanteringApplicationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Test
    void contextLoads() {
    }

    @Test
    @WithMockUser(username = "handler1", roles = {"NURSE"})
    void uiRequest_createsAuditEvent() throws Exception {
        MockMvc mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        long before = auditEventRepository.count();

        // Must be a mutation (POST/PUT/DELETE) to trigger auditing
        mockMvc.perform(post("/ui/cases/new").with(csrf()))
                .andExpect(status().isOk()); // Returns 200 (re-renders form) because body is missing

        long after = auditEventRepository.count();
        assertThat(after).isGreaterThan(before);
    }

}

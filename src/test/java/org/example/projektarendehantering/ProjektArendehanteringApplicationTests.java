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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @WithMockUser(username = "handler1", roles = {"HANDLER"})
    void uiRequest_createsAuditEvent() throws Exception {
        MockMvc mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        long before = auditEventRepository.count();

        mockMvc.perform(get("/ui/cases"))
                .andExpect(status().isOk());

        long after = auditEventRepository.count();
        assertThat(after).isGreaterThan(before);
    }

}

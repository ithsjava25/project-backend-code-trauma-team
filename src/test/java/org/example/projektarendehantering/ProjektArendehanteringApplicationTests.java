package org.example.projektarendehantering;

import org.example.projektarendehantering.infrastructure.persistence.AuditEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjektArendehanteringApplicationTests {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void repositoryIsAccessible() {
        long count = auditEventRepository.count();
        assertThat(count).isGreaterThanOrEqualTo(0);
    }

}

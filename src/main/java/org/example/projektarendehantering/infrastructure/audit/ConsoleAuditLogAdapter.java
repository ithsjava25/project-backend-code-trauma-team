package org.example.projektarendehantering.infrastructure.audit;

import org.example.projektarendehantering.application.ports.AuditLogPort;
import org.example.projektarendehantering.domain.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ConsoleAuditLogAdapter implements AuditLogPort {

    private static final Logger log = LoggerFactory.getLogger(ConsoleAuditLogAdapter.class);

    @Override
    public void append(AuditEvent event) {
        Objects.requireNonNull(event, "event");
        log.info("AUDIT {}", event);
    }
}


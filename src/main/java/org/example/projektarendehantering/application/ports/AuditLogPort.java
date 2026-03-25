package org.example.projektarendehantering.application.ports;

import org.example.projektarendehantering.domain.AuditEvent;

public interface AuditLogPort {

    void append(AuditEvent event);
}


package org.example.projektarendehantering.application.ports;

import org.example.projektarendehantering.domain.CaseEvent;

public interface CaseEventPublisherPort {

    void publishCaseEvent(CaseEvent event);
}


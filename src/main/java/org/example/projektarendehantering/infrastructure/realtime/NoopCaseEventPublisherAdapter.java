package org.example.projektarendehantering.infrastructure.realtime;

import org.example.projektarendehantering.application.ports.CaseEventPublisherPort;
import org.example.projektarendehantering.domain.CaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class NoopCaseEventPublisherAdapter implements CaseEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(NoopCaseEventPublisherAdapter.class);

    @Override
    public void publishCaseEvent(CaseEvent event) {
        Objects.requireNonNull(event, "event");
        // No-op stub for now; a real implementation can wire WebSocket/SSE delivery later.
        log.debug("CaseEvent (noop) {}", event);
    }
}


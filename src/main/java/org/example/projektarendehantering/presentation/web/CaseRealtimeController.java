package org.example.projektarendehantering.presentation.web;

import lombok.RequiredArgsConstructor;
import org.example.projektarendehantering.application.service.CaseRealtimeService;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/ui/realtime/cases")
@RequiredArgsConstructor
public class CaseRealtimeController {

    private final CaseRealtimeService caseRealtimeService;
    private final SecurityActorAdapter securityActorAdapter;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCaseList() {
        Actor actor = securityActorAdapter.currentUser();
        return caseRealtimeService.subscribeToCaseList(actor);
    }

    @GetMapping(path = "/{caseId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCase(@PathVariable UUID caseId) {
        Actor actor = securityActorAdapter.currentUser();
        return caseRealtimeService.subscribeToCase(actor, caseId);
    }
}

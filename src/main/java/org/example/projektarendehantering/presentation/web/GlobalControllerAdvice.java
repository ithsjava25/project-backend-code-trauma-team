package org.example.projektarendehantering.presentation.web;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final SecurityActorAdapter securityActorAdapter;

    public GlobalControllerAdvice(SecurityActorAdapter securityActorAdapter) {
        this.securityActorAdapter = securityActorAdapter;
    }

    @ModelAttribute("currentActor")
    public Actor currentActor() {
        try {
            return securityActorAdapter.currentUser();
        } catch (Exception e) {
            return null;
        }
    }
}

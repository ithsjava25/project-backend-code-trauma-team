package org.example.projektarendehantering.cases.presentation;

import org.example.projektarendehantering.shared.Actor;
import org.example.projektarendehantering.shared.NotAuthorizedException;
import org.example.projektarendehantering.identity.infrastructure.SecurityActorAdapter;
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
        } catch (NotAuthorizedException e) {
            return null;
        }
    }
}

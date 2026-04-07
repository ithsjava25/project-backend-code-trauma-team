package org.example.projektarendehantering.infrastructure.security;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Bridges Spring Security authentication to the application's Actor model.
 */
@Component
public class HeaderCurrentUserAdapter {

    public Actor currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new NotAuthorizedException("User not authenticated");
        }

        // Create a deterministic UUID based on the username/name
        UUID userId = UUID.nameUUIDFromBytes(authentication.getName().getBytes(StandardCharsets.UTF_8));

        Role role = Role.PATIENT;
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
            role = Role.MANAGER;
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
            role = Role.DOCTOR;
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_NURSE"))) {
            role = Role.NURSE;
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            role = Role.PATIENT;
        }

        return new Actor(userId, role);
    }
}


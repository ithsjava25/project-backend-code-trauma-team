package org.example.projektarendehantering.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.example.projektarendehantering.application.ports.CurrentUserPort;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Reads the current user identity from HTTP headers.
 * <p>
 * Expected headers:
 * - {@code X-User-Id}: UUID string
 * - {@code X-Role}: must match {@link Role} enum constant names exactly
 */
@Component
public class HeaderCurrentUserAdapter implements CurrentUserPort {

    private final HttpServletRequest request;

    public HeaderCurrentUserAdapter(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Actor currentUser() {
        String userIdHeader = request.getHeader("X-User-Id");
        String roleHeader = request.getHeader("X-Role");

        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new NotAuthorizedException("Missing header: X-User-Id");
        }
        if (roleHeader == null || roleHeader.isBlank()) {
            throw new NotAuthorizedException("Missing header: X-Role");
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new NotAuthorizedException("Invalid header: X-User-Id");
        }

        Role role;
        try {
            role = Role.valueOf(roleHeader);
        } catch (IllegalArgumentException e) {
            // Role.valueOf requires an exact match to enum constant names.
            throw new NotAuthorizedException("Invalid header: X-Role");
        }

        return new Actor(userId, role);
    }
}


package org.example.projektarendehantering.infrastructure.security;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Bridges Spring Security authentication to the application's Actor model.
 */
@Component
public class SecurityActorAdapter {

    private final EmployeeRepository employeeRepository;

    public SecurityActorAdapter(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Actor currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new NotAuthorizedException("User not authenticated");
        }

        // Try to find the username (GitHub 'login' attribute) or fallback to name
        String name = authentication.getName();
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            String login = oauth2Token.getPrincipal().getAttribute("login");
            if (login != null) {
                name = login;
            }
        }

        // Create a deterministic UUID based on the username/name
        UUID userId = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));

        // 1. Try finding an employee with this UUID
        var employee = employeeRepository.findById(userId);
        if (employee.isPresent()) {
            var e = employee.get();
            return new Actor(userId, e.getRole(), e.getDisplayName(), e.getGithubUsername());
        }

        // 2. Fallback to existing logic (checking Spring authorities)
        Role role = Role.PENDING;
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
            role = Role.MANAGER;
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
            role = Role.DOCTOR;
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_NURSE"))) {
            role = Role.NURSE;
        }

        return new Actor(userId, role, null, name);
    }
}

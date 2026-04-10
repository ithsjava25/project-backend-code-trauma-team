package org.example.projektarendehantering.common;

import java.util.UUID;

public record Actor(UUID userId, Role role, String displayName, String githubUsername) {

    public boolean isManager() {
        return role == Role.MANAGER;
    }

    public boolean isDoctor() {
        return role == Role.DOCTOR;
    }

    public boolean isNurse() {
        return role == Role.NURSE;
    }

    public boolean isPending() {
        return role == Role.PENDING;
    }
}

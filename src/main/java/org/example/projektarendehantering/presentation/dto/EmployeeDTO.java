package org.example.projektarendehantering.presentation.dto;

import org.example.projektarendehantering.common.Role;

import java.time.Instant;
import java.util.UUID;

public class EmployeeDTO {

    private UUID id;
    private String displayName;
    private Role role;
    private Instant createdAt;

    public EmployeeDTO() {}

    public EmployeeDTO(UUID id, String displayName, Role role, Instant createdAt) {
        this.id = id;
        this.displayName = displayName;
        this.role = role;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}


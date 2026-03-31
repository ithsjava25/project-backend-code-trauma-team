package org.example.projektarendehantering.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.example.projektarendehantering.common.Role;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "employees")
public class EmployeeEntity {

    @Id
    private UUID id;

    private String displayName;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Instant createdAt;

    public EmployeeEntity() {}

    public EmployeeEntity(UUID id, String displayName, Role role, Instant createdAt) {
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


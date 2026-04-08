package org.example.projektarendehantering.identity.domain;

import jakarta.persistence.*;
import org.example.projektarendehantering.shared.Role;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    private UUID id;

    @Column(unique = true)
    private String githubUsername;

    private String displayName;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Instant createdAt;

    public AccountEntity() {}

    public AccountEntity(UUID id, String githubUsername, String displayName, Role role, Instant createdAt) {
        this.id = id;
        this.githubUsername = githubUsername;
        this.displayName = displayName;
        this.role = role;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getGithubUsername() { return githubUsername; }
    public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

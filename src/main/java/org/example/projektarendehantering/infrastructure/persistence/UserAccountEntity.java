package org.example.projektarendehantering.infrastructure.persistence;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.projektarendehantering.common.Role;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_account")
@Getter
@Setter
public class UserAccountEntity {

    @Id
    private UUID id;

    @Email @Column(unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @NotNull @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column(name = "provider_user_id")
    private String providerUserId;

    @NotNull private boolean enabled = true;

    @NotNull @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void onPrePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}


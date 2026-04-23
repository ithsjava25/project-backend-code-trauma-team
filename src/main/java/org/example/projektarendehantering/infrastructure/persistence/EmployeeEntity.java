package org.example.projektarendehantering.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.projektarendehantering.common.Role;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "employees")
public class EmployeeEntity {

    @Id
    private UUID id;

    private String displayName;

    @Column(unique = true)
    private String githubUsername;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Instant createdAt;

}

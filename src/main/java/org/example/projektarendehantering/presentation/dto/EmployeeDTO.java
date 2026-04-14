package org.example.projektarendehantering.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projektarendehantering.common.Role;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private UUID id;
    private String displayName;
    private String githubUsername;
    private Role role;
    private Instant createdAt;

}

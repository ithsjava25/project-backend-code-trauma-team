package org.example.projektarendehantering.identity.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.projektarendehantering.shared.Role;

public class EmployeeCreateDTO {

    @NotBlank
    private String displayName;

    @NotBlank
    private String githubUsername;

    @NotNull
    private Role role;

    public EmployeeCreateDTO() {}

    public EmployeeCreateDTO(String displayName, String githubUsername, Role role) {
        this.displayName = displayName;
        this.githubUsername = githubUsername;
        this.role = role;
    }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getGithubUsername() { return githubUsername; }
    public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}


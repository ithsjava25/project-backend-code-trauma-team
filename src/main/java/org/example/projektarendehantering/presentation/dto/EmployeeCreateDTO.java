package org.example.projektarendehantering.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.projektarendehantering.common.Role;

public class EmployeeCreateDTO {

    @NotBlank
    private String displayName;

    @NotNull
    private Role role;

    public EmployeeCreateDTO() {}

    public EmployeeCreateDTO(String displayName, Role role) {
        this.displayName = displayName;
        this.role = role;
    }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}


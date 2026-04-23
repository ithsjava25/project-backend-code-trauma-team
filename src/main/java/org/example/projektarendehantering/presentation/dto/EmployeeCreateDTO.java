package org.example.projektarendehantering.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projektarendehantering.common.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateDTO {

    @NotBlank private String displayName;

    @NotBlank private String githubUsername;

    @NotNull private Role role;

}

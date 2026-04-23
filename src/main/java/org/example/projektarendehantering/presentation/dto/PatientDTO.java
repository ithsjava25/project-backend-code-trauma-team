package org.example.projektarendehantering.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {

    private UUID id;

    @NotBlank private String firstName;

    @NotBlank private String lastName;

    private String personalIdentityNumber;

    private Instant createdAt;

}

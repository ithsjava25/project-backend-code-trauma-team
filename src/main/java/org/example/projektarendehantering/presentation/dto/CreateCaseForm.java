package org.example.projektarendehantering.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCaseForm {

    @NotBlank(message = "Title is required") @Size(max = 200, message = "Title must be under 200 characters") private String title;

    @NotBlank(message = "Description is required") @Size(max = 4000, message = "Description must be under 4000 characters") private String description;

    private UUID patientId;

}

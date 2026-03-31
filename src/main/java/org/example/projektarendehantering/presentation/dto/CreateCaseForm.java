package org.example.projektarendehantering.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CreateCaseForm {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be under 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 4000, message = "Description must be under 4000 characters")
    private String description;

    @NotNull(message = "Patient is required")
    private UUID patientId;

    public CreateCaseForm() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
}

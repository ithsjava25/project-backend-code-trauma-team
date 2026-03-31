package org.example.projektarendehantering.presentation.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public class PatientDTO {

    private UUID id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String personalIdentityNumber;

    private Instant createdAt;

    public PatientDTO() {}

    public PatientDTO(UUID id, String firstName, String lastName, String personalIdentityNumber, Instant createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.personalIdentityNumber = personalIdentityNumber;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPersonalIdentityNumber() { return personalIdentityNumber; }
    public void setPersonalIdentityNumber(String personalIdentityNumber) { this.personalIdentityNumber = personalIdentityNumber; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}


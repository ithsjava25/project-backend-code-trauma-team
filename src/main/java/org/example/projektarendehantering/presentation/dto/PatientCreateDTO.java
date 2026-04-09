package org.example.projektarendehantering.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PatientCreateDTO {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Pattern(regexp = "^\\d{8}-\\d{4}$", message = "Use format YYYYMMDD-XXXX")
    private String personalIdentityNumber;

    public PatientCreateDTO() {}

    public PatientCreateDTO(String firstName, String lastName, String personalIdentityNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.personalIdentityNumber = personalIdentityNumber;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPersonalIdentityNumber() { return personalIdentityNumber; }
    public void setPersonalIdentityNumber(String personalIdentityNumber) { this.personalIdentityNumber = personalIdentityNumber; }
}


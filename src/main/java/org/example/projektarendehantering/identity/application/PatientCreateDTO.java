package org.example.projektarendehantering.identity.application;

import jakarta.validation.constraints.NotBlank;

public class PatientCreateDTO {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

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


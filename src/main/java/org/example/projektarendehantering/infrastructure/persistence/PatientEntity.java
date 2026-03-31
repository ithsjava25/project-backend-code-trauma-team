package org.example.projektarendehantering.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patients")
public class PatientEntity {

    @Id
    private UUID id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String personalIdentityNumber;

    private Instant createdAt;

    public PatientEntity() {}

    public PatientEntity(UUID id, String firstName, String lastName, String personalIdentityNumber, Instant createdAt) {
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


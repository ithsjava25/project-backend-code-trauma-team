package org.example.projektarendehantering.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "patient_profiles")
public class PatientProfileEntity {

    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private AccountEntity account;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String personalIdentityNumber;

    public PatientProfileEntity() {}

    public PatientProfileEntity(AccountEntity account, String firstName, String lastName, String personalIdentityNumber) {
        this.account = account;
        // Do NOT manually set this.id when using @MapsId
        this.firstName = firstName;
        this.lastName = lastName;
        this.personalIdentityNumber = personalIdentityNumber;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public AccountEntity getAccount() { return account; }
    public void setAccount(AccountEntity account) { this.account = account; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPersonalIdentityNumber() { return personalIdentityNumber; }
    public void setPersonalIdentityNumber(String personalIdentityNumber) { this.personalIdentityNumber = personalIdentityNumber; }
}

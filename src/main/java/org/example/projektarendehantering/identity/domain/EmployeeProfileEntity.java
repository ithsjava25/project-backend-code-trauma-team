package org.example.projektarendehantering.identity.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "employee_profiles")
public class EmployeeProfileEntity {

    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private AccountEntity account;

    private String employeeNumber;

    public EmployeeProfileEntity() {}

    public EmployeeProfileEntity(AccountEntity account) {
        this.account = account;
        // Do NOT manually set this.id when using @MapsId
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public AccountEntity getAccount() { return account; }
    public void setAccount(AccountEntity account) { this.account = account; }

    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
}

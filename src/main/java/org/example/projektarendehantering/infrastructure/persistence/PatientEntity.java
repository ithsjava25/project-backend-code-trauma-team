package org.example.projektarendehantering.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "patients")
public class PatientEntity {

    @Id
    private UUID id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String personalIdentityNumber;

    @OneToOne
    @JoinColumn(name = "user_account_id", unique = true)
    private UserAccountEntity userAccount;

    private Instant createdAt;

}

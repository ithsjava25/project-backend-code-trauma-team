package org.example.projektarendehantering.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<PatientEntity, UUID> {
    Optional<PatientEntity> findByPersonalIdentityNumber(String personalIdentityNumber);
    Optional<PatientEntity> findByUserAccount_Id(UUID userAccountId);
}


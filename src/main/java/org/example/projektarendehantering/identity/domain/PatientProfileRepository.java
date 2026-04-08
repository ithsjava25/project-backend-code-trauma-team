package org.example.projektarendehantering.identity.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PatientProfileRepository extends JpaRepository<PatientProfileEntity, UUID> {
    Optional<PatientProfileEntity> findByPersonalIdentityNumber(String personalIdentityNumber);
}

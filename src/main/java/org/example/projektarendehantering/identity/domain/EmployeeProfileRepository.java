package org.example.projektarendehantering.identity.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfileEntity, UUID> {
}

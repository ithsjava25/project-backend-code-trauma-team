package org.example.projektarendehantering.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfileEntity, UUID> {
}

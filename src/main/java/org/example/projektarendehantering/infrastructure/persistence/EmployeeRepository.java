package org.example.projektarendehantering.infrastructure.persistence;

import org.example.projektarendehantering.common.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {
    Optional<EmployeeEntity> findByGithubUsername(String githubUsername);
    List<EmployeeEntity> findAllByRole(Role role);
}


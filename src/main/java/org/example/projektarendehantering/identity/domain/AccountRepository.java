package org.example.projektarendehantering.identity.domain;

import org.example.projektarendehantering.shared.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    Optional<AccountEntity> findByGithubUsername(String githubUsername);
    List<AccountEntity> findAllByRole(Role role);
}

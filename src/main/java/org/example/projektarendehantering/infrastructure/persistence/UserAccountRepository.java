package org.example.projektarendehantering.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccountEntity, UUID> {

    Optional<UserAccountEntity> findByEmail(String email);

    Optional<UserAccountEntity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}

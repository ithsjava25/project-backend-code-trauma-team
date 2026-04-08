package org.example.projektarendehantering.identity.application;

import org.example.projektarendehantering.identity.domain.AccountEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
public class EmployeeMapper {

    public EmployeeDTO toDTO(AccountEntity entity) {
        if (entity == null) return null;
        return new EmployeeDTO(
                entity.getId(),
                entity.getDisplayName(),
                entity.getGithubUsername(),
                entity.getRole(),
                entity.getCreatedAt()
        );
    }

    public AccountEntity toEntity(EmployeeCreateDTO dto) {
        if (dto == null) return null;
        
        UUID id = null;
        if (dto.getGithubUsername() != null && !dto.getGithubUsername().isBlank()) {
            id = UUID.nameUUIDFromBytes(dto.getGithubUsername().getBytes(StandardCharsets.UTF_8));
        }

        AccountEntity entity = new AccountEntity();
        entity.setId(id);
        entity.setDisplayName(dto.getDisplayName());
        entity.setGithubUsername(dto.getGithubUsername());
        entity.setRole(dto.getRole());
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}

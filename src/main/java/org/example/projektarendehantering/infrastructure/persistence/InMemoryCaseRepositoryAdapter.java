package org.example.projektarendehantering.infrastructure.persistence;

import org.example.projektarendehantering.application.ports.CaseRepositoryPort;
import org.example.projektarendehantering.domain.Case;
import org.example.projektarendehantering.domain.CaseId;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryCaseRepositoryAdapter implements CaseRepositoryPort, CaseRepository {

    private final ConcurrentMap<CaseId, Case> domainCases = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, CaseEntity> entities = new ConcurrentHashMap<>();

    // Old Port implementation (for compatibility)
    @Override
    public Case save(Case caseToSave) {
        Objects.requireNonNull(caseToSave, "caseToSave");
        domainCases.put(caseToSave.id(), caseToSave);
        return caseToSave;
    }

    // New Repository implementation
    @Override
    public CaseEntity save(CaseEntity entity) {
        Objects.requireNonNull(entity, "entity");
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        entities.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<CaseEntity> findById(UUID id) {
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public List<CaseEntity> findAll() {
        return new ArrayList<>(entities.values());
    }
}

package org.example.projektarendehantering.infrastructure.persistence;

import org.example.projektarendehantering.application.ports.CaseRepositoryPort;
import org.example.projektarendehantering.domain.Case;
import org.example.projektarendehantering.domain.CaseId;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@Repository
public class InMemoryCaseRepositoryAdapter implements CaseRepositoryPort {

    private final ConcurrentMap<CaseId, Case> domainCases = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, CaseEntity> entities = new ConcurrentHashMap<>();

    // Old Port implementation (for compatibility)
    @Override
    public Case save(Case caseToSave) {
        Objects.requireNonNull(caseToSave, "caseToSave");
        domainCases.put(caseToSave.id(), caseToSave);
        return caseToSave;
    }

    // New Repository implementation (keeping the methods but not implementing CaseRepository)
    public CaseEntity save(CaseEntity entity) {
        Objects.requireNonNull(entity, "entity");
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        entities.put(entity.getId(), entity);
        return entity;
    }

    public Optional<CaseEntity> findById(UUID id) {
        return Optional.ofNullable(entities.get(id));
    }

    public boolean existsById(UUID uuid) {
        return false;
    }

    public <S extends CaseEntity> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    public List<CaseEntity> findAll() {
        return new ArrayList<>(entities.values());
    }

    public List<CaseEntity> findAllById(Iterable<UUID> uuids) {
        return List.of();
    }

    public long count() {
        return 0;
    }

    public void deleteById(UUID uuid) {

    }

    public void delete(CaseEntity entity) {

    }

    public void deleteAllById(Iterable<? extends UUID> uuids) {

    }

    public void deleteAll(Iterable<? extends CaseEntity> entities) {

    }

    public void deleteAll() {

    }

    public void flush() {

    }

    public <S extends CaseEntity> S saveAndFlush(S entity) {
        return null;
    }

    public <S extends CaseEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    public void deleteAllInBatch(Iterable<CaseEntity> entities) {

    }

    public void deleteAllByIdInBatch(Iterable<UUID> uuids) {

    }

    public void deleteAllInBatch() {

    }

    public CaseEntity getOne(UUID uuid) {
        return null;
    }

    public CaseEntity getById(UUID uuid) {
        return null;
    }

    public CaseEntity getReferenceById(UUID uuid) {
        return null;
    }

    public <S extends CaseEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    public <S extends CaseEntity> List<S> findAll(Example<S> example) {
        return List.of();
    }

    public <S extends CaseEntity> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    public <S extends CaseEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    public <S extends CaseEntity> long count(Example<S> example) {
        return 0;
    }

    public <S extends CaseEntity> boolean exists(Example<S> example) {
        return false;
    }

    public <S extends CaseEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    public List<CaseEntity> findAll(Sort sort) {
        return List.of();
    }

    public Page<CaseEntity> findAll(Pageable pageable) {
        return null;
    }
}

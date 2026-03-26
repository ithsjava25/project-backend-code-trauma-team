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
    public boolean existsById(UUID uuid) {
        return false;
    }

    @Override
    public <S extends CaseEntity> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public List<CaseEntity> findAll() {
        return new ArrayList<>(entities.values());
    }

    @Override
    public List<CaseEntity> findAllById(Iterable<UUID> uuids) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(UUID uuid) {

    }

    @Override
    public void delete(CaseEntity entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends UUID> uuids) {

    }

    @Override
    public void deleteAll(Iterable<? extends CaseEntity> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends CaseEntity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends CaseEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void deleteAllInBatch(Iterable<CaseEntity> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<UUID> uuids) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public CaseEntity getOne(UUID uuid) {
        return null;
    }

    @Override
    public CaseEntity getById(UUID uuid) {
        return null;
    }

    @Override
    public CaseEntity getReferenceById(UUID uuid) {
        return null;
    }

    @Override
    public <S extends CaseEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends CaseEntity> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends CaseEntity> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends CaseEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends CaseEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends CaseEntity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends CaseEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public List<CaseEntity> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<CaseEntity> findAll(Pageable pageable) {
        return null;
    }
}

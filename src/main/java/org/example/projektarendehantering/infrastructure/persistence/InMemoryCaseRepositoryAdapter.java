package org.example.projektarendehantering.infrastructure.persistence;

import org.example.projektarendehantering.application.ports.CaseRepositoryPort;
import org.example.projektarendehantering.domain.Case;
import org.example.projektarendehantering.domain.CaseId;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryCaseRepositoryAdapter implements CaseRepositoryPort {

    private final ConcurrentMap<CaseId, Case> cases = new ConcurrentHashMap<>();

    @Override
    public Case save(Case caseToSave) {
        Objects.requireNonNull(caseToSave, "caseToSave");
        cases.put(caseToSave.id(), caseToSave);
        return caseToSave;
    }
}


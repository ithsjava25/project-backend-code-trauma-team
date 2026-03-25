package org.example.projektarendehantering.application.ports;

import org.example.projektarendehantering.domain.Case;

public interface CaseRepositoryPort {

    Case save(Case caseToSave);
}


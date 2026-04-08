package org.example.projektarendehantering.cases.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CaseNoteRepository extends JpaRepository<CaseNoteEntity, UUID> {
}

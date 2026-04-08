package org.example.projektarendehantering.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CaseRepository extends JpaRepository<CaseEntity, UUID> {
    List<CaseEntity> findAllBySubject_Id(UUID subjectId);
    List<CaseEntity> findAllByOwnerId(UUID ownerId);
    List<CaseEntity> findAllByHandlerId(UUID handlerId);
    List<CaseEntity> findAllByOtherId(UUID otherId);
}

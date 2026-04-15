package org.example.projektarendehantering.infrastructure.persistence;

import org.example.projektarendehantering.common.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CaseRepository extends JpaRepository<CaseEntity, UUID> {
    List<CaseEntity> findAllByStatusNot(CaseStatus status);
    List<CaseEntity> findAllByPatient_Id(UUID patientId);
    List<CaseEntity> findAllByPatient_IdAndStatusNot(UUID patientId, CaseStatus status);
    List<CaseEntity> findAllByOwnerId(UUID ownerId);
    List<CaseEntity> findAllByOwnerIdAndStatusNot(UUID ownerId, CaseStatus status);
    List<CaseEntity> findAllByHandlerId(UUID handlerId);
    List<CaseEntity> findAllByHandlerIdAndStatusNot(UUID handlerId, CaseStatus status);
    List<CaseEntity> findAllByOtherId(UUID otherId);
}

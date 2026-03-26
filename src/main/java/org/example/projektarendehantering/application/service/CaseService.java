package org.example.projektarendehantering.application.service;

import org.example.projektarendehantering.infrastructure.persistence.CaseEntity;
import org.example.projektarendehantering.infrastructure.persistence.CaseRepository;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CaseService {

    private final CaseRepository caseRepository;
    private final CaseMapper caseMapper;

    public CaseService(CaseRepository caseRepository, CaseMapper caseMapper) {
        this.caseRepository = caseRepository;
        this.caseMapper = caseMapper;
    }

    @Transactional
    public CaseDTO createCase(CaseDTO caseDTO) {
        CaseEntity entity = caseMapper.toEntity(caseDTO);
        if (entity.getStatus() == null) {
            entity.setStatus("OPEN");
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        CaseEntity savedEntity = caseRepository.save(entity);
        return caseMapper.toDTO(savedEntity);
    }

    @Transactional(readOnly = true)
    public Optional<CaseDTO> getCase(UUID id) {
        return caseRepository.findById(id).map(caseMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<CaseDTO> getAllCases() {
        return caseRepository.findAll().stream()
                .map(caseMapper::toDTO)
                .collect(Collectors.toList());
    }
}

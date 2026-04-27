package org.example.projektarendehantering.application.service;

import lombok.RequiredArgsConstructor;
import org.example.projektarendehantering.infrastructure.persistence.CaseRepository;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeRepository;
import org.example.projektarendehantering.infrastructure.persistence.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PublicStatsService {

    private final CaseRepository caseRepository;
    private final PatientRepository patientRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Map<String, Long> getPublicStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalCases", caseRepository.count());
        stats.put("totalPatients", patientRepository.count());
        stats.put("totalEmployees", employeeRepository.count());
        return stats;
    }
}

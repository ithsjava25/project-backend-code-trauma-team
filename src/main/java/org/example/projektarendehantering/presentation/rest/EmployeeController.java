package org.example.projektarendehantering.presentation.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.projektarendehantering.application.service.EmployeeService;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.example.projektarendehantering.presentation.dto.EmployeeCreateDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeUpdateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final SecurityActorAdapter securityActorAdapter;

    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody @Valid EmployeeCreateDTO dto) {
        return ResponseEntity.ok(employeeService.createEmployee(securityActorAdapter.currentUser(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable UUID id, @RequestBody @Valid EmployeeUpdateDTO dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(securityActorAdapter.currentUser(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        employeeService.deleteEmployee(securityActorAdapter.currentUser(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable UUID id) {
        return employeeService.getEmployee(securityActorAdapter.currentUser(), id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees(securityActorAdapter.currentUser()));
    }
}

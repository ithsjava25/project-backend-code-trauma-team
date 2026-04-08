package org.example.projektarendehantering.identity.presentation;

import jakarta.validation.Valid;
import org.example.projektarendehantering.identity.application.EmployeeCreateDTO;
import org.example.projektarendehantering.identity.application.EmployeeDTO;
import org.example.projektarendehantering.identity.application.EmployeeService;
import org.example.projektarendehantering.identity.infrastructure.SecurityActorAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final SecurityActorAdapter securityActorAdapter;

    public EmployeeController(EmployeeService employeeService, SecurityActorAdapter securityActorAdapter) {
        this.employeeService = employeeService;
        this.securityActorAdapter = securityActorAdapter;
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody @Valid EmployeeCreateDTO dto) {
        return ResponseEntity.ok(employeeService.createEmployee(securityActorAdapter.currentUser(), dto));
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


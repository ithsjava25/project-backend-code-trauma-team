package org.example.projektarendehantering.presentation.rest;

import jakarta.validation.Valid;
import org.example.projektarendehantering.application.service.EmployeeService;
import org.example.projektarendehantering.presentation.dto.EmployeeCreateDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody @Valid EmployeeCreateDTO dto) {
        return ResponseEntity.ok(employeeService.createEmployee(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable UUID id) {
        return employeeService.getEmployee(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }
}


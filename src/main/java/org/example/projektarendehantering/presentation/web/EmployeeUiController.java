package org.example.projektarendehantering.presentation.web;

import jakarta.validation.Valid;
import org.example.projektarendehantering.application.service.EmployeeService;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.example.projektarendehantering.presentation.dto.EmployeeCreateDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeDTO;
import org.example.projektarendehantering.presentation.dto.EmployeeUpdateDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@Controller
public class EmployeeUiController {

    private final EmployeeService employeeService;
    private final SecurityActorAdapter securityActorAdapter;

    public EmployeeUiController(EmployeeService employeeService, SecurityActorAdapter securityActorAdapter) {
        this.employeeService = employeeService;
        this.securityActorAdapter = securityActorAdapter;
    }

    @GetMapping("/ui/employees")
    @PreAuthorize("hasRole('MANAGER')")
    public String listEmployees(Model model) {
        Actor actor = securityActorAdapter.currentUser();
        model.addAttribute("employees", employeeService.getAllEmployees(actor));
        return "employees/list";
    }

    @GetMapping("/ui/employees/new")
    @PreAuthorize("hasRole('MANAGER')")
    public String newEmployee(Model model) {
        model.addAttribute("employeeCreateDTO", new EmployeeCreateDTO());
        model.addAttribute("roles", Role.values());
        return "employees/new";
    }

    @PostMapping("/ui/employees/new")
    @PreAuthorize("hasRole('MANAGER')")
    public String createEmployee(@Valid @ModelAttribute("employeeCreateDTO") EmployeeCreateDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "employees/new";
        }

        Actor actor = securityActorAdapter.currentUser();
        employeeService.createEmployee(actor, dto);
        return "redirect:/ui/employees";
    }

    @GetMapping("/ui/employees/edit/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public String editEmployee(@PathVariable UUID id, Model model) {
        Actor actor = securityActorAdapter.currentUser();
        EmployeeDTO employee = employeeService.getEmployee(actor, id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id:" + id));

        EmployeeUpdateDTO updateDto = EmployeeUpdateDTO.builder()
                .displayName(employee.getDisplayName())
                .githubUsername(employee.getGithubUsername())
                .role(employee.getRole())
                .build();

        model.addAttribute("employeeUpdateDTO", updateDto);
        model.addAttribute("employeeId", id);
        model.addAttribute("roles", Role.values());
        return "employees/edit";
    }

    @PostMapping("/ui/employees/update/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public String updateEmployee(@PathVariable UUID id, @Valid @ModelAttribute("employeeUpdateDTO") EmployeeUpdateDTO dto, BindingResult result, Model model) {
        Actor actor = securityActorAdapter.currentUser();
        if (result.hasErrors()) {
            model.addAttribute("employeeId", id);
            model.addAttribute("roles", Role.values());
            return "employees/edit";
        }

        employeeService.updateEmployee(actor, id, dto);
        return "redirect:/ui/employees";
    }

    @GetMapping("/ui/employees/delete/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public String deleteEmployee(@PathVariable UUID id) {
        Actor actor = securityActorAdapter.currentUser();
        employeeService.deleteEmployee(actor, id);
        return "redirect:/ui/employees";
    }
}

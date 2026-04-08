package org.example.projektarendehantering.presentation.web;

import jakarta.validation.Valid;
import org.example.projektarendehantering.application.service.EmployeeService;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.example.projektarendehantering.presentation.dto.EmployeeCreateDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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
}

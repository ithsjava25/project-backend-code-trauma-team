package org.example.projektarendehantering.presentation.web;

import jakarta.validation.Valid;
import org.example.projektarendehantering.application.service.PatientService;
import org.example.projektarendehantering.presentation.dto.PatientCreateDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PatientUiController {

    private final PatientService patientService;

    public PatientUiController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/ui/patients")
    @PreAuthorize("hasRole('MANAGER')")
    public String listPatients(Model model) {
        model.addAttribute("patients", patientService.getAllPatients());
        return "patients/list";
    }

    @GetMapping("/ui/patients/new")
    @PreAuthorize("hasRole('MANAGER')")
    public String newPatient(Model model) {
        model.addAttribute("patientCreateDTO", new PatientCreateDTO());
        return "patients/new";
    }

    @PostMapping("/ui/patients/new")
    @PreAuthorize("hasRole('MANAGER')")
    public String createPatient(@Valid @ModelAttribute("patientCreateDTO") PatientCreateDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            return "patients/new";
        }

        patientService.createPatient(dto);
        return "redirect:/ui/patients";
    }
}

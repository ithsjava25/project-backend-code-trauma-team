package org.example.projektarendehantering.presentation.web;

import jakarta.validation.Valid;
import org.example.projektarendehantering.application.service.PatientService;
import org.example.projektarendehantering.common.BadRequestException;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.example.projektarendehantering.presentation.dto.PatientCreateDTO;
import org.example.projektarendehantering.presentation.dto.PatientDTO;
import org.example.projektarendehantering.presentation.dto.PatientUpdateDTO;
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
public class PatientUiController {

    private final PatientService patientService;
    private final SecurityActorAdapter securityActorAdapter;

    public PatientUiController(PatientService patientService, SecurityActorAdapter securityActorAdapter) {
        this.patientService = patientService;
        this.securityActorAdapter = securityActorAdapter;
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

    @GetMapping("/ui/patients/edit/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public String editPatient(@PathVariable UUID id, Model model) {
        PatientDTO patient = patientService.getPatient(id)
                .orElseThrow(() -> new BadRequestException("PATIENT_NOT_FOUND", "Invalid patient Id:" + id));
        
        PatientUpdateDTO updateDto = PatientUpdateDTO.builder()
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .personalIdentityNumber(patient.getPersonalIdentityNumber())
                .build();
        
        model.addAttribute("patientUpdateDTO", updateDto);
        model.addAttribute("patientId", id);
        return "patients/edit";
    }

    @PostMapping("/ui/patients/update/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public String updatePatient(@PathVariable UUID id, @Valid @ModelAttribute("patientUpdateDTO") PatientUpdateDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("patientId", id);
            return "patients/edit";
        }

        patientService.updatePatient(securityActorAdapter.currentUser(), id, dto);
        return "redirect:/ui/patients";
    }

    @GetMapping("/ui/patients/delete/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public String deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(securityActorAdapter.currentUser(), id);
        return "redirect:/ui/patients";
    }
}

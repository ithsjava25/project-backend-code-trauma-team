package org.example.projektarendehantering.presentation.web;

import org.example.projektarendehantering.application.service.CaseService;
import org.example.projektarendehantering.application.service.PatientService;
import org.example.projektarendehantering.infrastructure.security.HeaderCurrentUserAdapter;
import org.example.projektarendehantering.presentation.dto.CaseDTO;
import org.example.projektarendehantering.presentation.dto.CreateCaseForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import java.util.UUID;

@Controller
public class UiController {

    private final CaseService caseService;
    private final PatientService patientService;
    private final HeaderCurrentUserAdapter currentUserAdapter;

    public UiController(CaseService caseService, PatientService patientService, HeaderCurrentUserAdapter currentUserAdapter) {
        this.caseService = caseService;
        this.patientService = patientService;
        this.currentUserAdapter = currentUserAdapter;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/ui/cases")
    public String listCases(Model model) {
        model.addAttribute("cases", caseService.getAllCases(currentUserAdapter.currentUser()));
        return "cases/list";
    }

    @GetMapping("/ui/cases/new")
    public String newCase(Model model) {
        model.addAttribute("createCaseForm", new CreateCaseForm());
        model.addAttribute("patients", patientService.getAllPatients());
        return "cases/new";
    }

    @PostMapping("/ui/cases/new")
    public String createCase(@Valid @ModelAttribute("createCaseForm") CreateCaseForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "cases/new";
        }

        CaseDTO caseDTO = new CaseDTO();
        caseDTO.setTitle(form.getTitle());
        caseDTO.setDescription(form.getDescription());
        caseDTO.setPatientId(form.getPatientId());

        caseService.createCase(currentUserAdapter.currentUser(), caseDTO);
        return "redirect:/ui/cases";
    }

    @GetMapping("/ui/cases/{caseId}")
    public String caseDetail(@PathVariable UUID caseId, Model model) {
        caseService.getCase(currentUserAdapter.currentUser(), caseId).ifPresent(c -> model.addAttribute("case", c));
        return "cases/detail";
    }
}


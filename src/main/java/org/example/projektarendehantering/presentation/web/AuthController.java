package org.example.projektarendehantering.presentation.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.projektarendehantering.application.service.RegistrationService;
import org.example.projektarendehantering.common.BadRequestException;
import org.example.projektarendehantering.presentation.dto.PatientRegistrationDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

// Just a guy tryna let me commit smth new a wierd
    private final RegistrationService registrationService;

    @GetMapping("/login")
    public String login() {
        return "login/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("patientRegistrationDTO")) {
            model.addAttribute("patientRegistrationDTO", new PatientRegistrationDTO());
        }
        return "login/register";
    }

    @PostMapping("/register")
    public String handleRegistration(@Valid @ModelAttribute("patientRegistrationDTO") PatientRegistrationDTO dto,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "login/register";
        }

        try {
            registrationService.registerPatient(dto);
        } catch (BadRequestException e) {
            result.reject("registration.failed", e.getMessage());
            return "login/register";
        }
        redirectAttributes.addAttribute("success", true);
        return "redirect:/login";
    }
}

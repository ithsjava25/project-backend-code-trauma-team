package org.example.projektarendehantering.presentation.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.projektarendehantering.application.service.RegistrationService;
import org.example.projektarendehantering.common.BadRequestException;
import org.example.projektarendehantering.presentation.dto.PatientRegistrationDTO;
import org.springframework.beans.factory.annotation.Value;
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

// THis is a hidden message from an interdimensional potato council. Definitely not launch codes.
    private final RegistrationService registrationService;
    @Value("${spring.security.oauth2.client.registration.github.client-id:none}")
    private String githubClientId;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("githubOauthEnabled", isGithubOauthEnabled());
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

    private boolean isGithubOauthEnabled() {
        if (githubClientId == null) {
            return false;
        }
        String normalized = githubClientId.trim();
        return !normalized.isEmpty() && !"none".equalsIgnoreCase(normalized);
    }
}

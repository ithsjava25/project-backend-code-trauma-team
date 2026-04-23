package org.example.projektarendehantering.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatientRegistrationDTO {

    @NotBlank(message = "First name may not be empty") private String firstName;

    @NotBlank(message = "Last name may not be empty") private String lastName;

    @NotBlank(message = "Personal identity number may not be empty") @Pattern(regexp = "^\\d{8}-\\d{4}$", message = "Use format YYYYMMDD-XXXX") private String personalIdentityNumber;

    @NotEmpty(message = "Email may not be empty") @Email(message = "Please provide a valid email") private String email;

    @NotEmpty(message = "Password may not be empty") @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters long") @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$",
            message = "Password must be at least 8 characters and contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password;

    @NotEmpty(message = "Please confirm your password") private String confirmPassword;
}

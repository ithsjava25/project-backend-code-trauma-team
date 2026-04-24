package org.example.projektarendehantering.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Resets tracked failure state for local login after successful authentication.
 */
@Component
public class LoginAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginAttemptService loginAttemptService;

    public LoginAuthenticationSuccessHandler(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        loginAttemptService.recordSuccess(authentication.getName(), request.getRemoteAddr());
        response.sendRedirect("/home");
    }
}

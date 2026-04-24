package org.example.projektarendehantering.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Records local-login failures and redirects with a generic error outcome.
 */
@Component
@Slf4j
public class LoginAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;

    public LoginAuthenticationFailureHandler(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        String clientIp = request.getRemoteAddr();
        LoginAttemptService.LockDecision decision = loginAttemptService.recordFailure(username, clientIp);

        String redirectUrl = "/login?error=true";
        if (decision.locked()) {
            redirectUrl = redirectUrl + "&locked=true&retryAfter=" + URLEncoder.encode(String.valueOf(decision.retryAfterSeconds()), StandardCharsets.UTF_8);
        }
        log.info("security_event=LOGIN_FAILURE_REDIRECT clientIp={} locked={} retryAfterSeconds={}",
                clientIp, decision.locked(), decision.retryAfterSeconds());
        response.sendRedirect(redirectUrl);
    }
}

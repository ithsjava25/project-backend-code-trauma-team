package org.example.projektarendehantering.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Blocks local login requests while lockout is active.
 */
@Component
@Slf4j
public class LoginLockoutFilter extends OncePerRequestFilter {

    private final LoginAttemptService loginAttemptService;

    public LoginLockoutFilter(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!isLocalLoginAttempt(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = request.getParameter("username");
        String clientIp = ClientIpResolver.resolve(request);
        LoginAttemptService.LockDecision decision = loginAttemptService.currentLockDecision(username, clientIp);
        if (!decision.locked()) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("security_event=LOGIN_LOCKOUT_BLOCKED clientIp={} username={} retryAfterSeconds={}",
                clientIp, username == null ? "" : username.trim().toLowerCase(), decision.retryAfterSeconds());
        String retryAfter = URLEncoder.encode(String.valueOf(decision.retryAfterSeconds()), StandardCharsets.UTF_8);
        response.sendRedirect("/login?error=true&locked=true&retryAfter=" + retryAfter);
    }

    private boolean isLocalLoginAttempt(HttpServletRequest request) {
        return "/login".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod());
    }
}

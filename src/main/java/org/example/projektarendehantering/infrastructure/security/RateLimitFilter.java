package org.example.projektarendehantering.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that enforces per-IP rate limiting policies for MVP Phase 1.
 *
 * <p>Policy mapping:
 * <ul>
 *     <li>`/api/**` -> global API policy</li>
 *     <li>`/login`, `/register` -> auth endpoint policy</li>
 * </ul>
 */
@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private final RateLimitService rateLimitService;
    private final SecurityObservabilityService securityObservabilityService;

    public RateLimitFilter(RateLimitService rateLimitService, SecurityObservabilityService securityObservabilityService) {
        this.rateLimitService = rateLimitService;
        this.securityObservabilityService = securityObservabilityService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        var policy = rateLimitService.policyForPath(requestPath, request.getMethod());
        if (policy.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        var decision = rateLimitService.evaluate(policy.get(), clientIp);
        if (decision.allowed()) {
            filterChain.doFilter(request, response);
            return;
        }

        securityObservabilityService.recordRateLimitDenied(policy.get().name());
        log.info("security_event=RATE_LIMIT_DENIED policy={} path={} method={} clientIp={} retryAfterSeconds={}",
                policy.get().name(), requestPath, request.getMethod(), clientIp, decision.retryAfterSeconds());

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(RETRY_AFTER_HEADER, String.valueOf(decision.retryAfterSeconds()));
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.getWriter().write("Too many requests. Please try again later.");
    }
}

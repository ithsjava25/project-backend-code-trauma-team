package org.example.projektarendehantering.audit.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.projektarendehantering.shared.Actor;
import org.example.projektarendehantering.audit.application.AuditService;
import org.example.projektarendehantering.audit.domain.AuditEventEntity;
import org.example.projektarendehantering.identity.infrastructure.SecurityActorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditService auditService;
    private final SecurityActorAdapter securityActorAdapter;

    public AuditInterceptor(AuditService auditService, SecurityActorAdapter securityActorAdapter) {
        this.auditService = auditService;
        this.securityActorAdapter = securityActorAdapter;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Actor actor = null;
        try {
            actor = securityActorAdapter.currentUser();
        } catch (RuntimeException ignored) {
            // If not authenticated (or adapter throws), we still avoid failing the request.
        }

        AuditEventEntity event = new AuditEventEntity();
        event.setOccurredAt(Instant.now());
        if (actor != null) {
            event.setActorId(actor.userId());
            event.setActorRole(actor.role() != null ? actor.role().name() : null);
        }
        event.setPrincipalName(request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null);

        event.setHttpMethod(request.getMethod());
        event.setRequestPath(request.getRequestURI());
        event.setQueryString(request.getQueryString());
        event.setHandler(handlerName(handler));

        event.setResponseStatus(response != null ? response.getStatus() : null);
        event.setErrorType(ex != null ? ex.getClass().getSimpleName() : null);

        event.setCaseId(extractCaseId(request));

        event.setClientIp(clientIp(request));
        event.setUserAgent(request.getHeader("User-Agent"));

        try {
            auditService.record(event);
        } catch (RuntimeException ignored) {
            // Audit should never break user flows.
        }
    }

    private String handlerName(Object handler) {
        if (handler instanceof HandlerMethod hm) {
            return hm.getBeanType().getSimpleName() + "#" + hm.getMethod().getName();
        }
        return handler != null ? handler.getClass().getSimpleName() : null;
    }

    private UUID extractCaseId(HttpServletRequest request) {
        Object attr = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(attr instanceof Map<?, ?> vars)) return null;

        Object caseId = vars.get("caseId");
        if (caseId == null) {
            caseId = vars.get("id");
        }
        if (caseId == null) return null;

        try {
            return UUID.fromString(caseId.toString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int comma = forwardedFor.indexOf(',');
            return (comma >= 0 ? forwardedFor.substring(0, comma) : forwardedFor).trim();
        }
        return request.getRemoteAddr();
    }
}

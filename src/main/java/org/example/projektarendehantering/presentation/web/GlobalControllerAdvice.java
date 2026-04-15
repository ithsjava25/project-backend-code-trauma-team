package org.example.projektarendehantering.presentation.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.AppException;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final SecurityActorAdapter securityActorAdapter;

    public GlobalControllerAdvice(SecurityActorAdapter securityActorAdapter) {
        this.securityActorAdapter = securityActorAdapter;
    }

    @ModelAttribute("currentActor")
    public Actor currentActor() {
        try {
            return securityActorAdapter.currentUser();
        } catch (NotAuthorizedException e) {
            return null;
        }
    }

    @ExceptionHandler(AppException.class)
    public Object handleAppException(AppException e, HttpServletRequest request, HttpServletResponse response, Model model) {
        HttpStatus status = getStatus(e);
        if (isRestRequest(request)) {
            return ResponseEntity.status(status)
                    .body(Map.of(
                            "errorCode", e.errorCode(),
                            "message", e.getMessage(),
                            "status", status.value()
                    ));
        }
        response.setStatus(status.value());
        model.addAttribute("status", status.value() + " " + status.getReasonPhrase());
        model.addAttribute("message", e.getMessage());
        model.addAttribute("errorCode", e.errorCode());
        return "error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request, HttpServletResponse response, Model model) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        if (isRestRequest(request)) {
            return ResponseEntity.status(status)
                    .body(Map.of(
                            "errorCode", "ACCESS_DENIED",
                            "message", "You do not have permission to access this resource.",
                            "status", status.value()
                    ));
        }
        response.setStatus(status.value());
        model.addAttribute("status", status.value() + " " + status.getReasonPhrase());
        model.addAttribute("message", "You do not have permission to access this resource.");
        model.addAttribute("errorCode", "ACCESS_DENIED");
        return "error";
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Object handleResponseStatusException(ResponseStatusException e, HttpServletRequest request, HttpServletResponse response, Model model) {
        HttpStatusCode code = e.getStatusCode();
        String reason = e.getReason() != null ? e.getReason() : "An error occurred";
        
        if (isRestRequest(request)) {
            return ResponseEntity.status(code)
                    .body(Map.of(
                            "message", reason,
                            "status", code.value()
                    ));
        }
        
        response.setStatus(code.value());
        String statusLabel = code.value() + "";
        if (code instanceof HttpStatus hs) {
            statusLabel += " " + hs.getReasonPhrase();
        }
        
        model.addAttribute("status", statusLabel);
        model.addAttribute("message", reason);
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneralException(Exception e, HttpServletRequest request, HttpServletResponse response, Model model) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (isRestRequest(request)) {
            return ResponseEntity.status(status)
                    .body(Map.of(
                            "errorCode", "INTERNAL_SERVER_ERROR",
                            "message", "An unexpected error occurred.",
                            "status", status.value()
                    ));
        }
        response.setStatus(status.value());
        model.addAttribute("status", status.value() + " " + status.getReasonPhrase());
        model.addAttribute("message", "An unexpected error occurred.");
        model.addAttribute("errorCode", "INTERNAL_SERVER_ERROR");
        return "error";
    }

    private boolean isRestRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    private HttpStatus getStatus(AppException e) {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
        return responseStatus != null ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}

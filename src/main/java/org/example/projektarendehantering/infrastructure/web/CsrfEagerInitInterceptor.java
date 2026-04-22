package org.example.projektarendehantering.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CsrfEagerInitInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Object csrfAttr = request.getAttribute(CsrfToken.class.getName());
        if (csrfAttr instanceof CsrfToken csrfToken) {
            csrfToken.getToken();
        }
        return true;
    }
}

package org.example.projektarendehantering.infrastructure.config;

import org.example.projektarendehantering.infrastructure.web.AuditInterceptor;
import org.example.projektarendehantering.infrastructure.web.CsrfEagerInitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuditWebMvcConfig implements WebMvcConfigurer {

    private final AuditInterceptor auditInterceptor;
    private final CsrfEagerInitInterceptor csrfEagerInitInterceptor;

    public AuditWebMvcConfig(AuditInterceptor auditInterceptor, CsrfEagerInitInterceptor csrfEagerInitInterceptor) {
        this.auditInterceptor = auditInterceptor;
        this.csrfEagerInitInterceptor = csrfEagerInitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(csrfEagerInitInterceptor);

        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/ui/**", "/api/**")
                .excludePathPatterns(
                        "/static/**",
                        "/app.css",
                        "/app.js",
                        "/error**",
                        "/login**"
                );
    }
}


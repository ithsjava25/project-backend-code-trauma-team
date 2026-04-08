package org.example.projektarendehantering.audit.infrastructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuditWebMvcConfig implements WebMvcConfigurer {

    private final AuditInterceptor auditInterceptor;

    public AuditWebMvcConfig(AuditInterceptor auditInterceptor) {
        this.auditInterceptor = auditInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
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

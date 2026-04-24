package org.example.projektarendehantering.infrastructure.config;

import org.example.projektarendehantering.infrastructure.security.CustomOAuth2UserService;
import org.example.projektarendehantering.infrastructure.security.LoginAuthenticationFailureHandler;
import org.example.projektarendehantering.infrastructure.security.LoginAuthenticationSuccessHandler;
import org.example.projektarendehantering.infrastructure.security.LoginLockoutFilter;
import org.example.projektarendehantering.infrastructure.security.LocalUserDetailsService;
import org.example.projektarendehantering.infrastructure.security.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final LocalUserDetailsService localUserDetailsService;

    public SecurityConfig(LocalUserDetailsService localUserDetailsService) {
        this.localUserDetailsService = localUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CustomOAuth2UserService customOAuth2UserService,
                                                   RateLimitFilter rateLimitFilter,
                                                   LoginLockoutFilter loginLockoutFilter,
                                                   LoginAuthenticationSuccessHandler loginAuthenticationSuccessHandler,
                                                   LoginAuthenticationFailureHandler loginAuthenticationFailureHandler) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login**", "/register", "/error**", "/static/**", "/app.css", "/app.js", "/webjars/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")
                    .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .defaultSuccessUrl("/home", true)
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(loginAuthenticationSuccessHandler)
                .failureHandler(loginAuthenticationFailureHandler)
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    request -> request.getRequestURI().startsWith("/api/")
                )
            )
            .userDetailsService(localUserDetailsService)
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(loginLockoutFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

/*    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
            .username("admin")
            .password("{noop}password") // {noop} means no password encoding (fine for dev)
            .roles("MANAGER")
            .build();
        return new InMemoryUserDetailsManager(admin);
    }
 */

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Supports both encoded passwords (e.g. {bcrypt}) and explicit dev-only {noop} seeds.
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

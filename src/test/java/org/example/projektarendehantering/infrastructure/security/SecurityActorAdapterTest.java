package org.example.projektarendehantering.infrastructure.security;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityActorAdapterTest {

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private SecurityActorAdapter securityActorAdapter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentUser_whenNotAuthenticated_shouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThatThrownBy(() -> securityActorAdapter.currentUser())
                .isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    void currentUser_whenAnonymousUser_shouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");

        assertThatThrownBy(() -> securityActorAdapter.currentUser())
                .isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    void currentUser_shouldReturnActorWithManagerRole() {
        String username = "manager-user";
        UUID userId = UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_MANAGER")))
            .when(authentication).getAuthorities();

        Actor actor = securityActorAdapter.currentUser();

        assertThat(actor.role()).isEqualTo(Role.MANAGER);
        assertThat(actor.userId()).isEqualTo(userId);
    }

    @Test
    void currentUser_shouldReturnActorWithDoctorRole() {
        String username = "doctor-user";
        UUID userId = UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")))
            .when(authentication).getAuthorities();

        Actor actor = securityActorAdapter.currentUser();

        assertThat(actor.role()).isEqualTo(Role.DOCTOR);
        assertThat(actor.userId()).isEqualTo(userId);
    }

    @Test
    void currentUser_shouldReturnActorWithNurseRole() {
        String username = "nurse-user";
        UUID userId = UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_NURSE")))
            .when(authentication).getAuthorities();

        Actor actor = securityActorAdapter.currentUser();

        assertThat(actor.role()).isEqualTo(Role.NURSE);
        assertThat(actor.userId()).isEqualTo(userId);
    }

    @Test
    void currentUser_whenNoRoles_shouldDefaultToPatient() {
        String username = "patient-user";
        UUID userId = UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        
        doReturn(Collections.emptyList()).when(authentication).getAuthorities();

        Actor actor = securityActorAdapter.currentUser();

        assertThat(actor.role()).isEqualTo(Role.PATIENT);
        assertThat(actor.userId()).isEqualTo(userId);
    }

    // Helper because getAuthorities() is wildcard
    @SuppressWarnings("unchecked")
    private org.mockito.stubbing.Stubber doReturn(Object value) {
        return org.mockito.Mockito.doReturn(value);
    }
}

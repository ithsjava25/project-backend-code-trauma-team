package org.example.projektarendehantering.infrastructure.security;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeEntity;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeRepository;
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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityActorAdapterTest {

    @Mock
    private EmployeeRepository employeeRepository;

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

        assertThrows(NotAuthorizedException.class, () -> securityActorAdapter.currentUser());
    }

    @Test
    void currentUser_whenAnonymousUser_shouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");

        assertThrows(NotAuthorizedException.class, () -> securityActorAdapter.currentUser());
    }

    @Test
    void currentUser_whenEmployeeFoundInRepository_shouldReturnActorFromEmployee() {
        String username = "testuser";
        UUID userId = UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));
        EmployeeEntity employee = new EmployeeEntity(userId, "Test User", username, Role.DOCTOR, Instant.now());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(employeeRepository.findById(userId)).thenReturn(Optional.of(employee));

        Actor actor = securityActorAdapter.currentUser();

        assertEquals(userId, actor.userId());
        assertEquals(Role.DOCTOR, actor.role());
        assertEquals("Test User", actor.displayName());
        assertEquals(username, actor.githubUsername());
    }

    @Test
    void currentUser_whenOAuth2Authentication_shouldUseLoginAttribute() {
        String login = "oauth-user";
        UUID userId = UUID.nameUUIDFromBytes(login.getBytes(StandardCharsets.UTF_8));
        
        OAuth2AuthenticationToken oauth2Token = mock(OAuth2AuthenticationToken.class);
        OAuth2User oauth2User = mock(OAuth2User.class);

        when(securityContext.getAuthentication()).thenReturn(oauth2Token);
        when(oauth2Token.isAuthenticated()).thenReturn(true);
        when(oauth2Token.getName()).thenReturn("some-other-name");
        when(oauth2Token.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("login")).thenReturn(login);
        
        when(employeeRepository.findById(userId)).thenReturn(Optional.empty());
        doReturn(Collections.emptyList()).when(oauth2Token).getAuthorities();

        Actor actor = securityActorAdapter.currentUser();

        assertEquals(userId, actor.userId());
        assertEquals(login, actor.githubUsername());
        assertEquals(Role.PATIENT, actor.role());
    }

    @Test
    void currentUser_whenEmployeeNotFound_shouldFallbackToAuthorities_Manager() {
        String username = "manager-user";
        UUID userId = UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(employeeRepository.findById(userId)).thenReturn(Optional.empty());
        
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_MANAGER")))
            .when(authentication).getAuthorities();

        Actor actor = securityActorAdapter.currentUser();

        assertEquals(Role.MANAGER, actor.role());
        assertEquals(username, actor.githubUsername());
        assertNull(actor.displayName());
    }

    @Test
    void currentUser_whenEmployeeNotFound_shouldFallbackToAuthorities_Doctor() {
        String username = "doctor-user";
        UUID userId = UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(employeeRepository.findById(userId)).thenReturn(Optional.empty());
        
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")))
            .when(authentication).getAuthorities();

        Actor actor = securityActorAdapter.currentUser();

        assertEquals(Role.DOCTOR, actor.role());
    }

    @Test
    void currentUser_whenEmployeeNotFound_shouldFallbackToAuthorities_Nurse() {
        String username = "nurse-user";
        UUID userId = UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(employeeRepository.findById(userId)).thenReturn(Optional.empty());
        
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_NURSE")))
            .when(authentication).getAuthorities();

        Actor actor = securityActorAdapter.currentUser();

        assertEquals(Role.NURSE, actor.role());
    }

    @Test
    void currentUser_whenEmployeeNotFoundAndNoRoles_shouldDefaultToPatient() {
        String username = "patient-user";
        UUID userId = UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(employeeRepository.findById(userId)).thenReturn(Optional.empty());
        
        doReturn(Collections.emptyList()).when(authentication).getAuthorities();

        Actor actor = securityActorAdapter.currentUser();

        assertEquals(Role.PATIENT, actor.role());
    }
}

package org.example.projektarendehantering.infrastructure.security;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityActorAdapterTest {

    @InjectMocks
    private SecurityActorAdapter securityActorAdapter;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void currentUser_shouldReturnManagerActor() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("managerUser");
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_MANAGER"));
        doReturn(authorities).when(auth).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(auth);

        Actor actor = securityActorAdapter.currentUser();

        assertThat(actor.role()).isEqualTo(Role.MANAGER);
        UUID expectedId = UUID.nameUUIDFromBytes("managerUser".getBytes(StandardCharsets.UTF_8));
        assertThat(actor.userId()).isEqualTo(expectedId);
    }

    @Test
    void currentUser_shouldReturnPatientActor_whenNoRoles() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("patientUser");
        doReturn(List.of()).when(auth).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(auth);

        Actor actor = securityActorAdapter.currentUser();

        assertThat(actor.role()).isEqualTo(Role.PATIENT);
    }

    @Test
    void currentUser_shouldThrowException_whenNotAuthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertThatThrownBy(() -> securityActorAdapter.currentUser())
                .isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    void currentUser_shouldThrowException_whenAuthIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThatThrownBy(() -> securityActorAdapter.currentUser())
                .isInstanceOf(NotAuthorizedException.class);
    }

    // Helper because getAuthorities() is wildcard
    @SuppressWarnings("unchecked")
    private org.mockito.stubbing.Stubber doReturn(Object value) {
        return org.mockito.Mockito.doReturn(value);
    }
}

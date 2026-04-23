package org.example.projektarendehantering.infrastructure.security;

import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeEntity;
import org.example.projektarendehantering.infrastructure.persistence.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        // We create an anonymous subclass to override the base user loading logic
        customOAuth2UserService = new CustomOAuth2UserService(employeeRepository) {
            @Override
            protected OAuth2User loadBaseUser(OAuth2UserRequest userRequest) {
                return mockOAuth2User();
            }
        };
    }

    private OAuth2User mockOAuth2User() {
        return new DefaultOAuth2User(
            Collections.emptyList(),
            Map.of("login", " TestUser ", "id", 123),
            "id"
        );
    }

    @Test
    void loadUser_whenUserExistsInDb_shouldAddRoleAuthority() {
        // Arrange
        String login = "testuser";
        UUID userId = UUID.nameUUIDFromBytes(login.getBytes(StandardCharsets.UTF_8));
        EmployeeEntity employee = new EmployeeEntity(userId, "Test User", login, Role.DOCTOR, Instant.now());
        
        when(employeeRepository.findByGithubUsername(login)).thenReturn(Optional.of(employee));

        OAuth2UserRequest request = mockOAuth2UserRequest();
        
        // Act
        OAuth2User result = customOAuth2UserService.loadUser(request);

        // Assert
        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_DOCTOR");
    }

    @Test
    void loadUser_whenUserNotFoundInDb_shouldNotAddRoleAuthority() {
        // Arrange
        String login = "testuser";
        
        when(employeeRepository.findByGithubUsername(login)).thenReturn(Optional.empty());

        OAuth2UserRequest request = mockOAuth2UserRequest();
        
        // Act
        OAuth2User result = customOAuth2UserService.loadUser(request);

        // Assert
        assertThat(result.getAuthorities()).isEmpty();
    }

    private OAuth2UserRequest mockOAuth2UserRequest() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("github")
                .clientId("id")
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationUri("http://auth")
                .tokenUri("http://token")
                .userInfoUri("http://user")
                .redirectUri("http://redirect")
                .userNameAttributeName("id")
                .build();
        
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "token", Instant.now(), Instant.now().plusSeconds(3600));
        
        return new OAuth2UserRequest(clientRegistration, accessToken);
    }
}

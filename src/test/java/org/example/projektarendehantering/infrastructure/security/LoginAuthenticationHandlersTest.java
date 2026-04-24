package org.example.projektarendehantering.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginAuthenticationHandlersTest {

    @Test
    void failureHandler_shouldRedirectWithLockedFlagWhenThresholdReached() throws Exception {
        LoginAttemptService loginAttemptService = mock(LoginAttemptService.class);
        when(loginAttemptService.recordFailure("user@example.com", "127.0.0.1"))
                .thenReturn(new LoginAttemptService.LockDecision(true, 300));
        LoginAuthenticationFailureHandler handler = new LoginAuthenticationFailureHandler(loginAttemptService);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setRemoteAddr("127.0.0.1");
        request.addParameter("username", "user@example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(request, response, new org.springframework.security.authentication.BadCredentialsException("bad"));

        assertThat(response.getRedirectedUrl()).isEqualTo("/login?error=true&locked=true&retryAfter=300");
    }

    @Test
    void successHandler_shouldClearAttemptsAndRedirectHome() throws Exception {
        LoginAttemptService loginAttemptService = mock(LoginAttemptService.class);
        LoginAuthenticationSuccessHandler handler = new LoginAuthenticationSuccessHandler(loginAttemptService);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("user@example.com", "pw");

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(loginAttemptService).recordSuccess("user@example.com", "127.0.0.1");
        assertThat(response.getRedirectedUrl()).isEqualTo("/home");
    }
}

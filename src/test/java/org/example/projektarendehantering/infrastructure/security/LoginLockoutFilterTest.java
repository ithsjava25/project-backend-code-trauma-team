package org.example.projektarendehantering.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginLockoutFilterTest {

    @Test
    void doFilter_shouldRedirectWhenLockIsActive() throws Exception {
        LoginAttemptService loginAttemptService = mock(LoginAttemptService.class);
        when(loginAttemptService.currentLockDecision("blocked@example.com", "127.0.0.1"))
                .thenReturn(new LoginAttemptService.LockDecision(true, 120));

        LoginLockoutFilter filter = new LoginLockoutFilter(loginAttemptService);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setRemoteAddr("127.0.0.1");
        request.addParameter("username", "blocked@example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getRedirectedUrl()).isEqualTo("/login?error=true&locked=true&retryAfter=120");
    }

    @Test
    void doFilter_shouldPassWhenNotLocked() throws Exception {
        LoginAttemptService loginAttemptService = mock(LoginAttemptService.class);
        when(loginAttemptService.currentLockDecision("ok@example.com", "10.0.0.10"))
                .thenReturn(new LoginAttemptService.LockDecision(false, 0));

        LoginLockoutFilter filter = new LoginLockoutFilter(loginAttemptService);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setRemoteAddr("10.0.0.10");
        request.addParameter("username", "ok@example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getRedirectedUrl()).isNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }
}

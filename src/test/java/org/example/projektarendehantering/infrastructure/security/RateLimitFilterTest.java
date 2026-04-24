package org.example.projektarendehantering.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitFilterTest {

    @Test
    void doFilterInternal_shouldAllowWhenNoPolicyApplies() throws Exception {
        RateLimitService rateLimitService = mock(RateLimitService.class);
        when(rateLimitService.policyForPath("/home", "GET")).thenReturn(Optional.empty());

        RateLimitFilter filter = new RateLimitFilter(rateLimitService, SecurityObservabilityService.noop());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/home");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(rateLimitService).policyForPath("/home", "GET");
    }

    @Test
    void doFilterInternal_shouldReturnTooManyRequestsWhenPolicyDenied() throws Exception {
        RateLimitService rateLimitService = mock(RateLimitService.class);
        when(rateLimitService.policyForPath("/api/cases", "GET"))
                .thenReturn(Optional.of(RateLimitService.RateLimitPolicy.GLOBAL_API));
        when(rateLimitService.evaluate(RateLimitService.RateLimitPolicy.GLOBAL_API, "127.0.0.1"))
                .thenReturn(new RateLimitService.RateLimitDecision(false, 0, 42));

        RateLimitFilter filter = new RateLimitFilter(rateLimitService, SecurityObservabilityService.noop());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/cases");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("42");
        assertThat(response.getContentAsString()).isEqualTo("Too many requests. Please try again later.");
        verify(rateLimitService).evaluate(RateLimitService.RateLimitPolicy.GLOBAL_API, "127.0.0.1");
    }

    @Test
    void doFilterInternal_shouldPassThroughWhenPolicyAllowed() throws Exception {
        RateLimitService rateLimitService = mock(RateLimitService.class);
        when(rateLimitService.policyForPath("/login", "POST"))
                .thenReturn(Optional.of(RateLimitService.RateLimitPolicy.AUTH_ENDPOINT));
        when(rateLimitService.evaluate(RateLimitService.RateLimitPolicy.AUTH_ENDPOINT, "10.0.0.5"))
                .thenReturn(new RateLimitService.RateLimitDecision(true, 9, 0));

        RateLimitFilter filter = new RateLimitFilter(rateLimitService, SecurityObservabilityService.noop());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setRemoteAddr("10.0.0.5");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingFilterChain chain = new RecordingFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.wasCalled).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    private static final class RecordingFilterChain extends MockFilterChain {
        private boolean wasCalled;

        @Override
        public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response)
                throws IOException, jakarta.servlet.ServletException {
            wasCalled = true;
        }
    }
}

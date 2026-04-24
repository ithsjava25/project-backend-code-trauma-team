package org.example.projektarendehantering.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    @Test
    void shouldUseForwardedClientIpWhenRemoteAddressIsTrustedProxy() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.10");
        request.addHeader("X-Forwarded-For", "203.0.113.9, 10.0.0.10");

        String resolved = ClientIpResolver.resolve(request);

        assertThat(resolved).isEqualTo("203.0.113.9");
    }

    @Test
    void shouldIgnoreForwardedHeaderWhenRemoteAddressIsUntrusted() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.20");
        request.addHeader("X-Forwarded-For", "203.0.113.9");

        String resolved = ClientIpResolver.resolve(request);

        assertThat(resolved).isEqualTo("198.51.100.20");
    }

    @Test
    void shouldFallbackToRemoteAddressWhenForwardedHeaderIsInvalid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "unknown, invalid-ip");

        String resolved = ClientIpResolver.resolve(request);

        assertThat(resolved).isEqualTo("127.0.0.1");
    }
}

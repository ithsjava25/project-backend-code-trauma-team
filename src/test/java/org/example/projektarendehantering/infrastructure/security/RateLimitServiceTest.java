package org.example.projektarendehantering.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitServiceTest {

    @Test
    void policyForPath_shouldMapApiAndAuthEndpoints() {
        RateLimitService service = new RateLimitService(2, 60, 1, 60, new MutableClock(Instant.parse("2026-04-24T10:00:00Z")));

        assertThat(service.policyForPath("/api/patients", "GET"))
                .contains(RateLimitService.RateLimitPolicy.GLOBAL_API);
        assertThat(service.policyForPath("/login", "GET")).isEmpty();
        assertThat(service.policyForPath("/login", "POST"))
                .contains(RateLimitService.RateLimitPolicy.AUTH_ENDPOINT);
        assertThat(service.policyForPath("/register", "POST"))
                .contains(RateLimitService.RateLimitPolicy.AUTH_ENDPOINT);
        assertThat(service.policyForPath("/app.css", "GET")).isEmpty();
    }

    @Test
    void evaluate_shouldAllowUntilLimitAndThenDenyWithRetryAfter() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-24T10:00:00Z"));
        RateLimitService service = new RateLimitService(2, 60, 1, 60, clock);

        var first = service.evaluate(RateLimitService.RateLimitPolicy.GLOBAL_API, "127.0.0.1");
        var second = service.evaluate(RateLimitService.RateLimitPolicy.GLOBAL_API, "127.0.0.1");
        var third = service.evaluate(RateLimitService.RateLimitPolicy.GLOBAL_API, "127.0.0.1");

        assertThat(first.allowed()).isTrue();
        assertThat(first.remainingRequests()).isEqualTo(1);
        assertThat(second.allowed()).isTrue();
        assertThat(second.remainingRequests()).isEqualTo(0);
        assertThat(third.allowed()).isFalse();
        assertThat(third.retryAfterSeconds()).isGreaterThan(0);
    }

    @Test
    void evaluate_shouldUseIndependentCountersPerPolicy() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-24T10:00:00Z"));
        RateLimitService service = new RateLimitService(1, 60, 2, 60, clock);

        var apiAllowed = service.evaluate(RateLimitService.RateLimitPolicy.GLOBAL_API, "127.0.0.1");
        var authAllowed = service.evaluate(RateLimitService.RateLimitPolicy.AUTH_ENDPOINT, "127.0.0.1");
        var apiDenied = service.evaluate(RateLimitService.RateLimitPolicy.GLOBAL_API, "127.0.0.1");

        assertThat(apiAllowed.allowed()).isTrue();
        assertThat(authAllowed.allowed()).isTrue();
        assertThat(apiDenied.allowed()).isFalse();
    }

    private static final class MutableClock extends Clock {
        private Instant current;

        private MutableClock(Instant initial) {
            this.current = initial;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return current;
        }
    }
}

package org.example.projektarendehantering.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    @Test
    void recordFailure_shouldLockAfterThreshold() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-24T11:00:00Z"));
        LoginAttemptService service = new LoginAttemptService(5, 900, 900, clock);

        for (int i = 0; i < 4; i++) {
            assertThat(service.recordFailure("user@example.com", "127.0.0.1").locked()).isFalse();
        }

        LoginAttemptService.LockDecision decision = service.recordFailure("user@example.com", "127.0.0.1");
        assertThat(decision.locked()).isTrue();
        assertThat(decision.retryAfterSeconds()).isEqualTo(900);
    }

    @Test
    void currentLockDecision_shouldUnlockAfterTtlExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-24T11:00:00Z"));
        LoginAttemptService service = new LoginAttemptService(2, 900, 900, clock);

        service.recordFailure("user@example.com", "127.0.0.1");
        service.recordFailure("user@example.com", "127.0.0.1");
        assertThat(service.currentLockDecision("user@example.com", "127.0.0.1").locked()).isTrue();

        clock.advanceSeconds(901);
        assertThat(service.currentLockDecision("user@example.com", "127.0.0.1").locked()).isFalse();
    }

    @Test
    void recordSuccess_shouldClearFailureState() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-24T11:00:00Z"));
        LoginAttemptService service = new LoginAttemptService(2, 900, 900, clock);

        service.recordFailure("user@example.com", "127.0.0.1");
        service.recordFailure("user@example.com", "127.0.0.1");
        assertThat(service.currentLockDecision("user@example.com", "127.0.0.1").locked()).isTrue();

        service.recordSuccess("user@example.com", "127.0.0.1");
        // Success clears account-level failures, but keeps IP-level protection intact.
        assertThat(service.currentLockDecision("user@example.com", "127.0.0.1").locked()).isTrue();
        assertThat(service.currentLockDecision("user@example.com", "127.0.0.2").locked()).isFalse();
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

        private void advanceSeconds(long seconds) {
            current = current.plusSeconds(seconds);
        }
    }
}

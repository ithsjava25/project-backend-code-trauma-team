package org.example.projektarendehantering.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Tracks login failures and applies temporary lockout for local form login.
 *
 * <p>Phase 2 MVP behavior:
 * <ul>
 *     <li>Failure threshold: 5 attempts in 15 minutes (configurable).</li>
 *     <li>Lock duration: fixed 15 minutes (configurable).</li>
 *     <li>Counters are in-memory and instance-local for MVP.</li>
 * </ul>
 */
@Service
@Slf4j
public class LoginAttemptService {

    private final ConcurrentMap<String, AttemptState> emailAttempts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AttemptState> ipAttempts = new ConcurrentHashMap<>();
    private final int failureThreshold;
    private final int failureWindowSeconds;
    private final int lockDurationSeconds;
    private final Clock clock;
    private final SecurityObservabilityService securityObservabilityService;

    public LoginAttemptService(
            @Value("${app.security.login-attempt.failure-threshold:5}") int failureThreshold,
            @Value("${app.security.login-attempt.failure-window-seconds:900}") int failureWindowSeconds,
            @Value("${app.security.login-attempt.lock-duration-seconds:900}") int lockDurationSeconds,
            SecurityObservabilityService securityObservabilityService) {
        this(failureThreshold, failureWindowSeconds, lockDurationSeconds, Clock.systemUTC(), securityObservabilityService);
    }

    LoginAttemptService(int failureThreshold, int failureWindowSeconds, int lockDurationSeconds, Clock clock) {
        this(failureThreshold, failureWindowSeconds, lockDurationSeconds, clock, SecurityObservabilityService.noop());
    }

    LoginAttemptService(int failureThreshold,
                        int failureWindowSeconds,
                        int lockDurationSeconds,
                        Clock clock,
                        SecurityObservabilityService securityObservabilityService) {
        this.failureThreshold = failureThreshold;
        this.failureWindowSeconds = failureWindowSeconds;
        this.lockDurationSeconds = lockDurationSeconds;
        this.clock = clock;
        this.securityObservabilityService = securityObservabilityService;
    }

    public LockDecision currentLockDecision(String email, String clientIp) {
        long now = nowEpochSecond();
        long emailRetry = retryAfterSeconds(emailAttempts.get(normalizeEmail(email)), now);
        long ipRetry = retryAfterSeconds(ipAttempts.get(normalizeIp(clientIp)), now);
        long retryAfter = Math.max(emailRetry, ipRetry);
        return new LockDecision(retryAfter > 0, retryAfter);
    }

    public LockDecision recordFailure(String email, String clientIp) {
        long now = nowEpochSecond();
        securityObservabilityService.recordLoginFailure();
        long emailRetry = registerFailure(emailAttempts, normalizeEmail(email), now);
        long ipRetry = registerFailure(ipAttempts, normalizeIp(clientIp), now);
        long retryAfter = Math.max(emailRetry, ipRetry);
        if (retryAfter > 0) {
            securityObservabilityService.recordLoginLocked();
            log.warn("security_event=LOGIN_LOCKED email={} clientIp={} retryAfterSeconds={}", normalizeEmail(email), normalizeIp(clientIp), retryAfter);
        } else {
            log.info("security_event=LOGIN_FAILED email={} clientIp={}", normalizeEmail(email), normalizeIp(clientIp));
        }
        securityObservabilityService.setActiveLoginLocks(countActiveLocks(now));
        return new LockDecision(retryAfter > 0, retryAfter);
    }

    public void recordSuccess(String email, String clientIp) {
        emailAttempts.remove(normalizeEmail(email));
        ipAttempts.remove(normalizeIp(clientIp));
        securityObservabilityService.recordLoginSuccessReset();
        securityObservabilityService.setActiveLoginLocks(countActiveLocks(nowEpochSecond()));
        log.info("security_event=LOGIN_SUCCESS_RESET email={} clientIp={}", normalizeEmail(email), normalizeIp(clientIp));
    }

    private long registerFailure(ConcurrentMap<String, AttemptState> stateMap, String key, long nowEpochSecond) {
        AttemptState state = stateMap.computeIfAbsent(key, ignored -> new AttemptState(nowEpochSecond));
        synchronized (state) {
            if (state.lockedUntilEpochSecond > nowEpochSecond) {
                return state.lockedUntilEpochSecond - nowEpochSecond;
            }

            if ((nowEpochSecond - state.windowStartEpochSecond) >= failureWindowSeconds) {
                state.windowStartEpochSecond = nowEpochSecond;
                state.failureCount = 0;
            }

            state.failureCount++;
            if (state.failureCount >= failureThreshold) {
                state.lockedUntilEpochSecond = nowEpochSecond + lockDurationSeconds;
                return lockDurationSeconds;
            }
            return 0;
        }
    }

    private long retryAfterSeconds(AttemptState state, long nowEpochSecond) {
        if (state == null) {
            return 0;
        }
        synchronized (state) {
            if (state.lockedUntilEpochSecond <= nowEpochSecond) {
                state.lockedUntilEpochSecond = 0;
                return 0;
            }
            return state.lockedUntilEpochSecond - nowEpochSecond;
        }
    }

    private long nowEpochSecond() {
        return Instant.now(clock).getEpochSecond();
    }

    private int countActiveLocks(long nowEpochSecond) {
        int emailLocks = countActiveLocks(emailAttempts, nowEpochSecond);
        int ipLocks = countActiveLocks(ipAttempts, nowEpochSecond);
        return emailLocks + ipLocks;
    }

    private int countActiveLocks(ConcurrentMap<String, AttemptState> stateMap, long nowEpochSecond) {
        int activeLocks = 0;
        for (AttemptState state : stateMap.values()) {
            synchronized (state) {
                if (state.lockedUntilEpochSecond > nowEpochSecond) {
                    activeLocks++;
                }
            }
        }
        return activeLocks;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeIp(String clientIp) {
        return clientIp == null ? "" : clientIp.trim();
    }

    public record LockDecision(boolean locked, long retryAfterSeconds) {
    }

    private static final class AttemptState {
        private long windowStartEpochSecond;
        private int failureCount;
        private long lockedUntilEpochSecond;

        private AttemptState(long windowStartEpochSecond) {
            this.windowStartEpochSecond = windowStartEpochSecond;
        }
    }
}

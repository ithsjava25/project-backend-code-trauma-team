package org.example.projektarendehantering.infrastructure.security;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

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

    private static final int ATTEMPT_CACHE_MAX_SIZE = 10_000;

    private final ConcurrentMap<String, AttemptState> emailAttempts;
    private final ConcurrentMap<String, AttemptState> ipAttempts;
    private final int failureThreshold;
    private final int failureWindowSeconds;
    private final int lockDurationSeconds;
    private final Clock clock;
    private final SecurityObservabilityService securityObservabilityService;
    private final String logPseudonymizationSalt;

    @Autowired
    public LoginAttemptService(
            @Value("${app.security.login-attempt.failure-threshold:5}") int failureThreshold,
            @Value("${app.security.login-attempt.failure-window-seconds:900}") int failureWindowSeconds,
            @Value("${app.security.login-attempt.lock-duration-seconds:900}") int lockDurationSeconds,
            @Value("${app.security.log-pseudonymization-salt:}") String logPseudonymizationSalt,
            SecurityObservabilityService securityObservabilityService) {
        this(failureThreshold, failureWindowSeconds, lockDurationSeconds, Clock.systemUTC(), logPseudonymizationSalt, securityObservabilityService);
    }

    LoginAttemptService(int failureThreshold, int failureWindowSeconds, int lockDurationSeconds, Clock clock) {
        this(failureThreshold, failureWindowSeconds, lockDurationSeconds, clock, "", SecurityObservabilityService.noop());
    }

    LoginAttemptService(int failureThreshold,
                        int failureWindowSeconds,
                        int lockDurationSeconds,
                        Clock clock,
                        String logPseudonymizationSalt,
                        SecurityObservabilityService securityObservabilityService) {
        this.failureThreshold = failureThreshold;
        this.failureWindowSeconds = failureWindowSeconds;
        this.lockDurationSeconds = lockDurationSeconds;
        this.clock = clock;
        this.securityObservabilityService = securityObservabilityService;
        this.logPseudonymizationSalt = logPseudonymizationSalt == null ? "" : logPseudonymizationSalt;
        long attemptStateTtlSeconds = (long) failureWindowSeconds + lockDurationSeconds;
        this.emailAttempts = Caffeine.newBuilder()
                .expireAfterAccess(attemptStateTtlSeconds, TimeUnit.SECONDS)
                .maximumSize(ATTEMPT_CACHE_MAX_SIZE)
                .<String, AttemptState>build()
                .asMap();
        this.ipAttempts = Caffeine.newBuilder()
                .expireAfterAccess(attemptStateTtlSeconds, TimeUnit.SECONDS)
                .maximumSize(ATTEMPT_CACHE_MAX_SIZE)
                .<String, AttemptState>build()
                .asMap();
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
        String pseudonymizedEmail = pseudonymizeEmailForLogs(email);
        String pseudonymizedIp = pseudonymizeIpForLogs(clientIp);
        if (retryAfter > 0) {
            securityObservabilityService.recordLoginLocked();
            log.warn("security_event=LOGIN_LOCKED email={} clientIp={} retryAfterSeconds={}", pseudonymizedEmail, pseudonymizedIp, retryAfter);
        } else {
            log.info("security_event=LOGIN_FAILED email={} clientIp={}", pseudonymizedEmail, pseudonymizedIp);
        }
        securityObservabilityService.setActiveLoginLocks(countActiveLocks(now));
        return new LockDecision(retryAfter > 0, retryAfter);
    }

    public void recordSuccess(String email, String clientIp) {
        emailAttempts.remove(normalizeEmail(email));
        // Do NOT clear IP-level counters on success: a legitimate login from a
        // shared/NATted IP would otherwise reset brute-force protection for
        // every other account on that IP.
        securityObservabilityService.recordLoginSuccessReset();
        securityObservabilityService.setActiveLoginLocks(countActiveLocks(nowEpochSecond()));
        log.info("security_event=LOGIN_SUCCESS_RESET email={} clientIp={}", pseudonymizeEmailForLogs(email), pseudonymizeIpForLogs(clientIp));
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

    private String pseudonymizeEmailForLogs(String email) {
        String sanitizedEmail = sanitizeForLogs(normalizeEmail(email));
        if (sanitizedEmail.isEmpty()) {
            return "";
        }

        int atIndex = sanitizedEmail.lastIndexOf('@');
        if (atIndex > 0 && atIndex < sanitizedEmail.length() - 1) {
            return "@".concat(sanitizedEmail.substring(atIndex + 1));
        }

        return "hash:".concat(pseudonymizeForLogs(sanitizedEmail, logPseudonymizationSalt));
    }

    private String pseudonymizeIpForLogs(String clientIp) {
        return "hash:".concat(pseudonymizeForLogs(normalizeIp(clientIp), logPseudonymizationSalt));
    }

    private String pseudonymizeForLogs(String value, String appSalt) {
        String sanitizedValue = sanitizeForLogs(value);
        if (sanitizedValue.isEmpty()) {
            return "";
        }
        String saltedValue = sanitizeForLogs(appSalt).concat("|").concat(sanitizedValue);
        return sha256Hex(saltedValue).substring(0, 12);
    }

    private String sanitizeForLogs(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        StringBuilder sanitized = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            if (!Character.isISOControl(c)) {
                sanitized.append(c);
            }
        }
        return sanitized.toString().trim();
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable for log pseudonymization", ex);
        }
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

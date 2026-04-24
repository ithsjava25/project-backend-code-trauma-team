package org.example.projektarendehantering.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory, IP-based rate limiter for MVP Phase 1.
 *
 * <p>Implementation notes:
 * <ul>
 *     <li>Uses fixed time windows per policy and IP key.</li>
 *     <li>Stores counters in memory, so state is per-node and reset on restart.</li>
 *     <li>Returns retry-after hints used by the web filter when request is denied.</li>
 * </ul>
 */
@Service
public class RateLimitService {

    private final ConcurrentMap<String, CounterWindow> counters = new ConcurrentHashMap<>();
    private final int globalApiLimit;
    private final int globalApiWindowSeconds;
    private final int authEndpointLimit;
    private final int authEndpointWindowSeconds;
    private final Clock clock;

    public RateLimitService(
            @Value("${app.security.rate-limit.global-api.limit:100}") int globalApiLimit,
            @Value("${app.security.rate-limit.global-api.window-seconds:60}") int globalApiWindowSeconds,
            @Value("${app.security.rate-limit.auth-endpoint.limit:10}") int authEndpointLimit,
            @Value("${app.security.rate-limit.auth-endpoint.window-seconds:60}") int authEndpointWindowSeconds) {
        this(globalApiLimit, globalApiWindowSeconds, authEndpointLimit, authEndpointWindowSeconds, Clock.systemUTC());
    }

    RateLimitService(int globalApiLimit,
                     int globalApiWindowSeconds,
                     int authEndpointLimit,
                     int authEndpointWindowSeconds,
                     Clock clock) {
        this.globalApiLimit = globalApiLimit;
        this.globalApiWindowSeconds = globalApiWindowSeconds;
        this.authEndpointLimit = authEndpointLimit;
        this.authEndpointWindowSeconds = authEndpointWindowSeconds;
        this.clock = clock;
    }

    public Optional<RateLimitPolicy> policyForPath(String requestPath, String httpMethod) {
        if (requestPath == null) {
            return Optional.empty();
        }
        if (requestPath.startsWith("/api/")) {
            return Optional.of(RateLimitPolicy.GLOBAL_API);
        }
        if ("POST".equalsIgnoreCase(httpMethod) && ("/login".equals(requestPath) || "/register".equals(requestPath))) {
            return Optional.of(RateLimitPolicy.AUTH_ENDPOINT);
        }
        return Optional.empty();
    }

    public RateLimitDecision evaluate(RateLimitPolicy policy, String clientIp) {
        PolicyConfig policyConfig = policyConfig(policy);
        long nowEpochSecond = Instant.now(clock).getEpochSecond();
        String counterKey = policy.name() + ":" + clientIp;
        CounterWindow window = counters.computeIfAbsent(counterKey, ignored -> new CounterWindow(nowEpochSecond));

        synchronized (window) {
            long elapsedSeconds = nowEpochSecond - window.windowStartEpochSecond;
            if (elapsedSeconds >= policyConfig.windowSeconds()) {
                window.windowStartEpochSecond = nowEpochSecond;
                window.requestCount = 0;
            }

            if (window.requestCount >= policyConfig.limit()) {
                long retryAfterSeconds = Math.max(1, policyConfig.windowSeconds() - (nowEpochSecond - window.windowStartEpochSecond));
                return new RateLimitDecision(false, 0, retryAfterSeconds);
            }

            window.requestCount++;
            int remaining = Math.max(0, policyConfig.limit() - window.requestCount);
            return new RateLimitDecision(true, remaining, 0);
        }
    }

    private PolicyConfig policyConfig(RateLimitPolicy policy) {
        return switch (policy) {
            case GLOBAL_API -> new PolicyConfig(globalApiLimit, globalApiWindowSeconds);
            case AUTH_ENDPOINT -> new PolicyConfig(authEndpointLimit, authEndpointWindowSeconds);
        };
    }

    public enum RateLimitPolicy {
        GLOBAL_API,
        AUTH_ENDPOINT
    }

    public record RateLimitDecision(boolean allowed, int remainingRequests, long retryAfterSeconds) {
    }

    private record PolicyConfig(int limit, int windowSeconds) {
    }

    private static final class CounterWindow {
        private long windowStartEpochSecond;
        private int requestCount;

        private CounterWindow(long windowStartEpochSecond) {
            this.windowStartEpochSecond = windowStartEpochSecond;
        }
    }
}

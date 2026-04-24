package org.example.projektarendehantering.infrastructure.security;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized security telemetry for Phase 3 metrics.
 *
 * <p>When a {@link MeterRegistry} is available, counters and gauges are published.
 * Without a registry, methods still work as no-ops for easy testing.
 */
@Service
public class SecurityObservabilityService {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger activeLoginLocks = new AtomicInteger();

    private SecurityObservabilityService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        if (meterRegistry != null) {
            Gauge.builder("security.login.active_locks", activeLoginLocks, AtomicInteger::get)
                    .description("Current number of active login lockouts")
                    .register(meterRegistry);
        }
    }

    @Autowired
    public SecurityObservabilityService(ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this(meterRegistryProvider.getIfAvailable());
    }

    public static SecurityObservabilityService noop() {
        return new SecurityObservabilityService((MeterRegistry) null);
    }

    public void recordRateLimitDenied(String policy) {
        incrementCounter("security.rate_limit.denied", "policy", policy);
    }

    public void recordLoginFailure() {
        incrementCounter("security.login.failures", null, null);
    }

    public void recordLoginLocked() {
        incrementCounter("security.login.locked", null, null);
    }

    public void recordLoginSuccessReset() {
        incrementCounter("security.login.success_reset", null, null);
    }

    public void setActiveLoginLocks(int activeLocks) {
        activeLoginLocks.set(Math.max(activeLocks, 0));
    }

    private void incrementCounter(String name, String tagKey, String tagValue) {
        if (meterRegistry == null) {
            return;
        }
        Counter counter = (tagKey == null)
                ? meterRegistry.counter(name)
                : meterRegistry.counter(name, tagKey, tagValue);
        counter.increment();
    }
}

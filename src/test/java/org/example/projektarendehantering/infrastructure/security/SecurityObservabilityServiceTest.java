package org.example.projektarendehantering.infrastructure.security;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityObservabilityServiceTest {

    @Test
    void shouldRecordCountersAndGauge() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        @SuppressWarnings("unchecked")
        ObjectProvider<io.micrometer.core.instrument.MeterRegistry> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(meterRegistry);

        SecurityObservabilityService service = new SecurityObservabilityService(provider);
        service.recordRateLimitDenied("GLOBAL_API");
        service.recordLoginFailure();
        service.recordLoginLocked();
        service.recordLoginSuccessReset();
        service.setActiveLoginLocks(3);

        assertThat(meterRegistry.get("security.rate_limit.denied").tag("policy", "GLOBAL_API").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("security.login.failures").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("security.login.locked").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("security.login.success_reset").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("security.login.active_locks").gauge().value()).isEqualTo(3.0);
    }
}

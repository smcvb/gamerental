package io.axoniq.demo.gamerental.diagnostics;

import org.axonframework.messaging.core.sequencing.PropertySequencingPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("diagnostics")
@Configuration
class DiagnosticsConfiguration {

    @Bean
    public PropertySequencingPolicy<StuckSegmentDemoEvent, String> stuckSegmentSequencingPolicy() {
        return new PropertySequencingPolicy<>(StuckSegmentDemoEvent.class, "segmentKey");
    }
}

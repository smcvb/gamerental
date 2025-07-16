package io.axoniq.demo.gamerental;

import jakarta.annotation.Nonnull;
import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.configuration.ComponentRegistry;
import org.axonframework.configuration.ConfigurationEnhancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public ConfigurationEnhancer contextSwitcher() {
        return new ConfigurationEnhancer() {
            @Override
            public void enhance(@Nonnull ComponentRegistry registry) {
                registry.registerComponent(AxonServerConfiguration.class, c -> {
                    AxonServerConfiguration axonConfig = new AxonServerConfiguration();
                    axonConfig.setContext("game-rental");
                    return axonConfig;
                });
            }

            @Override
            public int order() {
                return Integer.MIN_VALUE;
            }
        };
    }
}

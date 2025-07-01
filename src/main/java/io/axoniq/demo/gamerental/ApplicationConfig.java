package io.axoniq.demo.gamerental;

import jakarta.annotation.Nonnull;
import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.configuration.ApplicationConfigurer;
import org.axonframework.configuration.ComponentRegistry;
import org.axonframework.configuration.ConfigurationEnhancer;
import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
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

    @Bean
    public static ApplicationConfigurer axonConfigurer(EventSourcedEntityModule<String, ?> gameModule) {
        return EventSourcingConfigurer.create()
                                      .componentRegistry(cr -> cr.registerModule(gameModule))
                                      .componentRegistry(cr -> cr.registerEnhancer(
                                              new ConfigurationEnhancer() {
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
                                              }
                                      ));
    }

    @Bean
    public org.axonframework.configuration.Configuration axonConfiguration(ApplicationConfigurer axonConfigurer) {
        return axonConfigurer.start();
    }

    @Bean
    public CommandGateway commandGateway(org.axonframework.configuration.Configuration axonConfiguration) {
        return axonConfiguration.getComponent(CommandGateway.class);
    }

    @Bean
    public QueryGateway queryGateway(org.axonframework.configuration.Configuration axonConfiguration) {
        return axonConfiguration.getComponent(QueryGateway.class);
    }

    @Bean
    public QueryUpdateEmitter queryUpdateEmitter(org.axonframework.configuration.Configuration axonConfiguration) {
        return axonConfiguration.getComponent(QueryUpdateEmitter.class);
    }
}

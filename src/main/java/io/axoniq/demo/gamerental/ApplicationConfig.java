package io.axoniq.demo.gamerental;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.configuration.ApplicationConfigurer;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.modelling.configuration.StatefulCommandHandlingModule;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public static ApplicationConfigurer axonConfigurer(StatefulCommandHandlingModule gameModule) {
        return EventSourcingConfigurer.create()
                                      .modelling(modelling -> modelling.registerStatefulCommandHandlingModule(
                                              gameModule
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

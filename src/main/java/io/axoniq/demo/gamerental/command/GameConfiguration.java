package io.axoniq.demo.gamerental.command;

import org.axonframework.eventsourcing.configuration.EventSourcedEntityBuilder;
import org.axonframework.modelling.configuration.StatefulCommandHandlingModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfiguration {

    @Bean
    public static StatefulCommandHandlingModule gameModule() {
        return StatefulCommandHandlingModule.named("game")
                                            .entities()
                                            .entity(EventSourcedEntityBuilder.annotatedEntity(String.class, Game.class))
                                            .commandHandlers()
                                            .annotatedCommandHandlingComponent(c -> new GameCommandHandler())
                                            .build();
    }
}

package io.axoniq.demo.gamerental.command;

import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfiguration {

    @Bean
    public static EventSourcedEntityModule<String, Game> gameEntityModule() {
        return EventSourcedEntityModule.annotated(String.class, Game.class);
    }
}

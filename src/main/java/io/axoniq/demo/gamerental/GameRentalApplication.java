package io.axoniq.demo.gamerental;

import io.axoniq.framework.messaging.eventhandling.deadletter.DeadLetterQueueConfiguration;
import org.axonframework.extension.spring.config.EventProcessorDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GameRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameRentalApplication.class, args);
    }

    @Bean
    public EventProcessorDefinition catalogEventProcessor() {
        return EventProcessorDefinition.pooledStreamingMatching("catalog").customized(
                psepConfig -> psepConfig.extend(DeadLetterQueueConfiguration.class, this::dlqConfig)
        );
    }

    @Bean
    public EventProcessorDefinition reservationsEventProcessor() {
        return EventProcessorDefinition.pooledStreamingMatching("reservations").customized(
                psepConfig -> psepConfig.extend(DeadLetterQueueConfiguration.class, this::dlqConfig)
        );
    }

    private DeadLetterQueueConfiguration dlqConfig() {
        return new DeadLetterQueueConfiguration()
                .cacheMaxSize(50);
    }
}

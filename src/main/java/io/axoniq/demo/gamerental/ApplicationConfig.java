package io.axoniq.demo.gamerental;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.ConfigurerModule;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.lifecycle.Phase;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.interceptors.LoggingInterceptor;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    @Qualifier("messageSerializer")
    public Serializer messageSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule())
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return JacksonSerializer.builder()
                                .objectMapper(objectMapper)
                                .lenientDeserialization()
                                .build();
    }

    @Bean
    public LoggingInterceptor<Message<?>> loggingInterceptor() {
        return new LoggingInterceptor<>();
    }

    /**
     * Using a {@link ConfigurerModule} provides a means to register components as part of the start and shutdown cycles
     * of Axon Framework. The {@link LoggingInterceptor} is registered in the latest phase on start-up for the buses.
     * Framework issue #2061 (https://github.com/AxonFramework/AxonFramework/pull/2061) would've simplified this
     * further, but 4.6.0 isn't finished yet.
     */
    @Bean
    public ConfigurerModule loggingInterceptorConfigurerModule(LoggingInterceptor<Message<?>> loggingInterceptor) {
        return configurer -> {
            configurer.onInitialize(config -> config.onStart(Phase.INSTRUCTION_COMPONENTS, () -> {
                CommandBus commandBus = config.commandBus();
                commandBus.registerDispatchInterceptor(loggingInterceptor);
                commandBus.registerHandlerInterceptor(loggingInterceptor);
                EventBus eventBus = config.eventBus();
                eventBus.registerDispatchInterceptor(loggingInterceptor);
                QueryBus queryBus = config.queryBus();
                queryBus.registerDispatchInterceptor(loggingInterceptor);
                queryBus.registerHandlerInterceptor(loggingInterceptor);
            }));
            configurer.eventProcessing()
                      .registerDefaultHandlerInterceptor((c, processorName) -> loggingInterceptor);
        };
    }
}

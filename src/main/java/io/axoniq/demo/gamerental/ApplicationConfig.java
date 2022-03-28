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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

@Configuration
public class ApplicationConfig {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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


    /**
     * This {@link Hooks#onErrorDropped(Consumer)} is included as a recommendation from RSocket Java's GitHub issue
     * [#1018](https://github.com/rsocket/rsocket-java/issues/1018). Ideally the problem would be taken care off by
     * RSocket, but at this stage the below solution is recommended by a contributor. To be certain not all exception
     * are blocked, only the {@code "Exceptions$ErrorCallbackNotImplemented"} is covered.
     */
    @Autowired
    public void configureHooks() {
        Hooks.onErrorDropped(t -> {
            if (!t.getClass().toString().equals("class reactor.core.Exceptions$ErrorCallbackNotImplemented")) {
                logger.warn("Invoked onErrorDropped for exception [{}]", t.getClass(), t);
            }
        });
    }
}

package io.axoniq.demo.gamerental;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.axonframework.commandhandling.distributed.UnresolvedRoutingKeyPolicy;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
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
import java.time.Duration;
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
    public RoutingStrategy routingStrategy() {
        return AnnotationRoutingStrategy.builder()
                                        .fallbackRoutingStrategy(UnresolvedRoutingKeyPolicy.RANDOM_KEY)
                                        .build();
    }

    @Bean
    public LoggingInterceptor<Message<?>> loggingInterceptor() {
        return new LoggingInterceptor<>();
    }

    @Autowired
    public void configureLoggingInterceptorFor(CommandBus commandBus,
                                               LoggingInterceptor<Message<?>> loggingInterceptor) {
        commandBus.registerDispatchInterceptor(loggingInterceptor);
        commandBus.registerHandlerInterceptor(loggingInterceptor);
    }

    @Autowired
    public void configureLoggingInterceptorFor(EventBus eventBus, LoggingInterceptor<Message<?>> loggingInterceptor) {
        eventBus.registerDispatchInterceptor(loggingInterceptor);
    }

    @Autowired
    public void configureLoggingInterceptorFor(EventProcessingConfigurer eventProcessingConfigurer,
                                               LoggingInterceptor<Message<?>> loggingInterceptor) {
        eventProcessingConfigurer.registerDefaultHandlerInterceptor((config, processorName) -> loggingInterceptor);
    }

    @Autowired
    public void configureLoggingInterceptorFor(QueryBus queryBus, LoggingInterceptor<Message<?>> loggingInterceptor) {
        queryBus.registerDispatchInterceptor(loggingInterceptor);
        queryBus.registerHandlerInterceptor(loggingInterceptor);
    }

    @Autowired
    public void configureResultHandlerInterceptors(ReactorCommandGateway commandGateway,
                                                   ReactorQueryGateway queryGateway) {
        commandGateway.registerResultHandlerInterceptor(
                (cmd, result) -> result.onErrorMap(ExceptionMapper::mapRemoteException)
        );

        queryGateway.registerResultHandlerInterceptor(
                (query, result) -> result.onErrorMap(ExceptionMapper::mapRemoteException)
        );
        queryGateway.registerResultHandlerInterceptor((query, result) -> result.timeout(Duration.ofMillis(500)));
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

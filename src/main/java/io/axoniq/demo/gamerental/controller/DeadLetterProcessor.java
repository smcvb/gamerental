package io.axoniq.demo.gamerental.controller;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.deadletter.SequencedDeadLetterProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Profile("reservations")
@Component
class DeadLetterProcessor {

    private final EventProcessingConfiguration processingConfiguration;
    private final ExecutorService executorService;

    public DeadLetterProcessor(EventProcessingConfiguration processingConfiguration) {
        this.processingConfiguration = processingConfiguration;
        this.executorService = Executors.newFixedThreadPool(5);
    }

    public CompletableFuture<Boolean> processorAnyFor(String processingGroup) {
        return CompletableFuture.completedFuture(true);
    }
}

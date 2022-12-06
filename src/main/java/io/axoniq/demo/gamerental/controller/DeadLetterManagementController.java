package io.axoniq.demo.gamerental.controller;


import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Profile("reservations")
@RestController
@RequestMapping("/dead-letter")
class DeadLetterManagementController {

    private final DeadLetterProcessor deadLetterProcessor;

    public DeadLetterManagementController(DeadLetterProcessor deadLetterProcessor) {
        this.deadLetterProcessor = deadLetterProcessor;
    }

    @PostMapping("/{processing-group}/any")
    public CompletableFuture<Boolean> processAny(@PathVariable("processing-group") String processingGroup) {
        return deadLetterProcessor.processorAnyFor(processingGroup);
    }
}

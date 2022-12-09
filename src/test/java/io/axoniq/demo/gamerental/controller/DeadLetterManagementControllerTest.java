package io.axoniq.demo.gamerental.controller;

import org.junit.jupiter.api.*;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

class DeadLetterManagementControllerTest {

    private DeadLetterProcessor deadLetterProcessor;

    private WebTestClient testClient;

    @BeforeEach
    void setUp() {
        deadLetterProcessor = mock(DeadLetterProcessor.class);

        testClient = WebTestClient.bindToController(new DeadLetterManagementController(deadLetterProcessor)).build();
    }

    @Test
    void anyEndpointInvokesProcessAnyFor() {
        String processingGroup = "some-processing-group";

        when(deadLetterProcessor.processorAnyFor(processingGroup)).thenReturn(CompletableFuture.completedFuture(true));

        testClient.post()
                  .uri(uriBuilder -> uriBuilder.path("/dead-letter/{processing-group}/any")
                                               .build(processingGroup))
                  .exchange()
                  .expectStatus().isOk()
                  .expectBody(Boolean.class)
                  .isEqualTo(true);

        verify(deadLetterProcessor).processorAnyFor(processingGroup);
    }
}
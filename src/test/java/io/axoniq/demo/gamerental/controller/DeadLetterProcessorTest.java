package io.axoniq.demo.gamerental.controller;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.deadletter.SequencedDeadLetterProcessor;
import org.junit.jupiter.api.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeadLetterProcessorTest {

    public static final String PROCESSING_GROUP_NAME = "some-processing-group-name";
    private EventProcessingConfiguration processingConfig;

    private DeadLetterProcessor testSubject;

    @BeforeEach
    void setUp() {
        processingConfig = mock(EventProcessingConfiguration.class);

        testSubject = new DeadLetterProcessor(processingConfig);
    }

    @Test
    void throwIllegalArgumentExceptionIfProcessingGroupDoesNotHaveSequencedDeadLetterProcessor() {
        when(processingConfig.sequencedDeadLetterProcessor(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> testSubject.processorAnyFor(PROCESSING_GROUP_NAME));
    }

    @Test
    void invokeProcessAnyForExistingSequencedDeadLetterProcessor() throws ExecutionException, InterruptedException {
        //noinspection unchecked
        SequencedDeadLetterProcessor<EventMessage<?>> letterProcessor = mock(SequencedDeadLetterProcessor.class);
        when(letterProcessor.processAny()).thenReturn(true);

        when(processingConfig.sequencedDeadLetterProcessor(PROCESSING_GROUP_NAME))
                .thenReturn(Optional.of(letterProcessor));

        CompletableFuture<Boolean> result = testSubject.processorAnyFor(PROCESSING_GROUP_NAME);

        try {
            assertTrue(result.get(500, TimeUnit.MILLISECONDS));
        } catch (TimeoutException e) {
            fail("We'd expect the DeadLetterProcessor to return sooner");
        }
        assertTrue(result.isDone());
        verify(letterProcessor).processAny();
    }
}
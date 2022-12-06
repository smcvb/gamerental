package io.axoniq.demo.gamerental;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.MetaData;
import org.axonframework.messaging.deadletter.DeadLetter;
import org.axonframework.messaging.deadletter.Decisions;
import org.axonframework.messaging.deadletter.EnqueueDecision;
import org.axonframework.messaging.deadletter.EnqueuePolicy;

public class RetryConstrainedEnqueuePolicy implements EnqueuePolicy<EventMessage<?>> {

    private static final String RETRY_COUNT_KEY = "retries";

    private final int retryConstraint;

    public RetryConstrainedEnqueuePolicy(int retryConstraint) {
        this.retryConstraint = retryConstraint;
    }

    @Override
    public EnqueueDecision<EventMessage<?>> decide(DeadLetter<? extends EventMessage<?>> letter, Throwable cause) {
        int retries = (int) letter.diagnostics().getOrDefault("retries", 0);
        if (retries == 0) {
            return Decisions.enqueue(cause, l -> MetaData.with(RETRY_COUNT_KEY, 0));
        } else if (retries > retryConstraint) {
            return Decisions.evict();
        } else {
            return Decisions.requeue(cause, l -> l.diagnostics().and(RETRY_COUNT_KEY, retries + 1));
        }
    }
}

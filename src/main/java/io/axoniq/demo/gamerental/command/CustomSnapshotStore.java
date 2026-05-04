package io.axoniq.demo.gamerental.command;

import org.axonframework.eventsourcing.snapshot.api.Snapshot;
import org.axonframework.eventsourcing.snapshot.store.SnapshotStore;
import org.axonframework.messaging.core.QualifiedName;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

// Custom SnapshotStore required for Axon Framework users **only**!
public class CustomSnapshotStore implements SnapshotStore {

    @Override
    public CompletableFuture<Void> store(QualifiedName qualifiedName,
                                         Object identifier,
                                         Snapshot snapshot) {
        // left out for simplicity...
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<@Nullable Snapshot> load(QualifiedName qualifiedName,
                                                      Object identifier) {
        // left out for simplicity...
        return CompletableFuture.completedFuture(null);
    }
}

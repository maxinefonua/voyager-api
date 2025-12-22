package org.voyager.api.model.path;

import lombok.Getter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Getter
public class Session<T> {
    private final String searchId;
    private final PathSearchRequest request;
    private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();
    private final AtomicInteger totalFound;
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private volatile boolean sessionComplete = false;
    private volatile boolean failed = false;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public Session(String searchId, PathSearchRequest request, int startingCount) {
        this.searchId = searchId;
        this.request = request;
        this.totalFound = new AtomicInteger(startingCount);
        this.createdAt = Instant.now();
        this.expiresAt = createdAt.plus(30, ChronoUnit.MINUTES); // 30min expiry
    }

    public void addResult(T result) {
        if (queue.offer(result)) {
            totalFound.getAndIncrement();
        }
    }

    public List<T> getResults() {
        List<T> batch = new ArrayList<>();
        queue.drainTo(batch);
        processedCount.getAndAdd(batch.size());
        return batch;
    }

    public boolean hasMore() {
        return !queue.isEmpty() || !sessionComplete;
    }

    public void markComplete() {
       this.sessionComplete = true;
    }

    public void markFailed() {
       this.failed = true;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
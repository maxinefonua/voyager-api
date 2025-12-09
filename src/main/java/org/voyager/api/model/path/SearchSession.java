package org.voyager.api.model.path;

import lombok.Getter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class SearchSession {
    private final String searchId;
    private final PathSearchRequest request;
    private final BlockingQueue<PathDetailed> resultsQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger totalFound;
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private volatile boolean searchComplete = false;
    private volatile boolean failed = false;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final Object lock = new Object();

    public SearchSession(PathSearchRequest request, String searchId, int directPathsCount) {
        this.searchId = searchId;
        this.request = request;
        this.totalFound = new AtomicInteger(directPathsCount);
        this.createdAt = Instant.now();
        this.expiresAt = createdAt.plus(30, ChronoUnit.MINUTES); // 30min expiry
    }

    public void addResult(PathDetailed pathDetailed) {
        if (resultsQueue.offer(pathDetailed)) {
            totalFound.incrementAndGet();
        }
    }

    public List<PathDetailed> getPollResults() {
        List<PathDetailed> batch = new ArrayList<>();
        while (!resultsQueue.isEmpty()) {
            PathDetailed pathDetailed = resultsQueue.poll();
            batch.add(pathDetailed);
            processedCount.incrementAndGet();
        }
        return batch;
    }

    public boolean hasMoreResults() {
        return !resultsQueue.isEmpty() || !searchComplete;
    }

    public void markComplete() {
        this.searchComplete = true;
    }

    public void markFailed() {
        this.failed = true;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}

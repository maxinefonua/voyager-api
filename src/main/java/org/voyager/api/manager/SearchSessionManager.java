package org.voyager.api.manager;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.model.path.SearchSession;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SearchSessionManager {
    private final Map<String, SearchSession> activeSearches = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor =
            Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        // Clean up expired sessions every 5 minutes
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredSessions, 5, 5, TimeUnit.MINUTES);
    }

    public boolean hasActiveSearches() {
        return !activeSearches.isEmpty();
    }

    public String createSession(PathSearchRequest request, int directPathsCount) {
        String searchId = UUID.randomUUID().toString();
        SearchSession session = new SearchSession(request,searchId,directPathsCount);
        activeSearches.put(searchId, session);
        return searchId;
    }

    public SearchSession getSession(String searchId) {
        SearchSession session = activeSearches.get(searchId);
        // TODO: handle correctly
        if (session == null || session.isExpired()) {
            activeSearches.remove(searchId);
            throw new IllegalArgumentException("Search session expired or not found");
        }
        return session;
    }

    public void completeSession(String searchId) {
        activeSearches.remove(searchId);
    }

    private void cleanupExpiredSessions() {
        Instant now = Instant.now();
        activeSearches.entrySet().removeIf(entry ->
                entry.getValue().isExpired() || now.isAfter(entry.getValue().getExpiresAt()));
    }
}
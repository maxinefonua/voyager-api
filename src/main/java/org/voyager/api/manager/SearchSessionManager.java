package org.voyager.api.manager;

import io.vavr.control.Option;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.model.path.SearchSession;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SearchSessionManager {
    private final Map<String, SearchSession> activeSearches = new ConcurrentHashMap<>();
    private final Map<String, SearchSession> completedSearches = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor =
            Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        // Clean up expired sessions every 5 minutes
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredSessions, 5, 5, TimeUnit.MINUTES);
    }

    public void createSession(PathSearchRequest request, String cacheKey, int directPathsCount) {
        SearchSession session = new SearchSession(request,cacheKey,directPathsCount);
        activeSearches.put(cacheKey, session);
    }

    public Option<SearchSession> getSession(String cacheKey) {
        SearchSession session = activeSearches.get(cacheKey);
        if (session == null) {
            if (completedSearches.containsKey(cacheKey)) {
                session = completedSearches.get(cacheKey);
                completedSearches.remove(cacheKey);
                return Option.of(session);
            }
            return Option.none();
        }
        if (session.isExpired() || session.isSearchComplete()) {
            activeSearches.remove(cacheKey);
        }
        return Option.of(session);
    }

    private void cleanupExpiredSessions() {
        Instant now = Instant.now();
        activeSearches.entrySet().removeIf(entry -> {
            boolean remove = entry.getValue().isExpired() || now.isAfter(entry.getValue().getExpiresAt());
            if (remove) {
                if (!entry.getValue().getResultsQueue().isEmpty()) {
                    completedSearches.put(entry.getKey(),entry.getValue());
                }
            }
            return remove;
        });
    }
}
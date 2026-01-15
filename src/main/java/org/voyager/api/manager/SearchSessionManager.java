package org.voyager.api.manager;

import io.vavr.control.Option;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.voyager.api.model.path.PathDetailed;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.model.path.Session;
import org.voyager.commons.model.path.Path;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SearchSessionManager {
    private final Map<String, Session<Path>> activeSearches = new ConcurrentHashMap<>();
    private final Map<String, Session<Path>> completedSearches = new ConcurrentHashMap<>();
    private final Map<String, Session<PathDetailed>> activeConversions = new ConcurrentHashMap<>();
    private final Map<String, Session<PathDetailed>> completedConversions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor =
            Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        // Clean up expired sessions every 5 minutes
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredSessions, 5, 5, TimeUnit.MINUTES);
    }

    public void createSearchSession(PathSearchRequest request, String pathCacheKey, int directPathsCount) {
        Session<Path> session = new Session<>(pathCacheKey,request,directPathsCount);
        activeSearches.put(pathCacheKey, session);
    }

    public void createConversionSession(PathSearchRequest request, String conversionCacheKey, int directPathsCount) {
        Session<PathDetailed> session = new Session<>(conversionCacheKey,request,directPathsCount);
        activeConversions.put(conversionCacheKey, session);
    }

    public Option<Session<Path>> getSearchSession(String pathCacheKey) {
        Session<Path> session = activeSearches.get(pathCacheKey);
        if (session == null) {
            if (completedSearches.containsKey(pathCacheKey)) {
                session = completedSearches.get(pathCacheKey);
                completedSearches.remove(pathCacheKey);
                return Option.of(session);
            }
            return Option.none();
        }
        if (session.isExpired() || session.isSessionComplete()) {
            activeSearches.remove(pathCacheKey);
        }
        return Option.of(session);
    }

    public Option<Session<PathDetailed>> getConversionSession(String conversionCacheKey) {
        Session<PathDetailed> session = activeConversions.get(conversionCacheKey);
        if (session == null) {
            if (completedConversions.containsKey(conversionCacheKey)) {
                session = completedConversions.get(conversionCacheKey);
                completedConversions.remove(conversionCacheKey);
                return Option.of(session);
            }
            return Option.none();
        }
        if (session.isExpired() || session.isSessionComplete()) {
            activeConversions.remove(conversionCacheKey);
        }
        return Option.of(session);
    }

    private void cleanupExpiredSessions() {
        Instant now = Instant.now();
        activeSearches.entrySet().removeIf(entry -> {
            boolean remove = entry.getValue().isExpired() || now.isAfter(entry.getValue().getExpiresAt());
            if (remove) {
                if (!entry.getValue().getQueue().isEmpty()) {
                    completedSearches.put(entry.getKey(),entry.getValue());
                }
            }
            return remove;
        });

        activeConversions.entrySet().removeIf(entry -> {
            boolean remove = entry.getValue().isExpired() || now.isAfter(entry.getValue().getExpiresAt());
            if (remove) {
                if (!entry.getValue().getQueue().isEmpty()) {
                    completedConversions.put(entry.getKey(),entry.getValue());
                }
            }
            return remove;
        });
    }
}
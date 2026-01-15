package org.voyager.api.auth;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.Bucket;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

public class ApiKeyAuthentication extends AbstractAuthenticationToken {
    private final String apiKey;

    public ApiKeyAuthentication(String apiKey, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.apiKey = apiKey;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return apiKey;
    }

    @Service
    public static class GlobalRateLimitService {
        // Global daily limit for ALL public users combined
        @Value("${public.daily.limit}")
        int publicDailyLimit;

        private static final Duration RESET_DURATION = Duration.ofMinutes(1);
        private static final Logger LOGGER = LoggerFactory.getLogger(GlobalRateLimitService.class);

        // Single bucket for all public users
        private final AtomicReference<Bucket> globalPublicBucket = new AtomicReference<>();

        @PostConstruct
        public void init() {
            resetGlobalBucket();
        }

        public boolean tryConsumeForPublicUser() {
            return globalPublicBucket.get().tryConsume(1);
        }

        public int getRemainingTokens() {
            return (int) globalPublicBucket.get().getAvailableTokens();
        }

        public void resetGlobalBucket() {
            LOGGER.info("Resetting global rate limiting bucket at {} with limit: {}",
                    LocalTime.now(),publicDailyLimit);

            Bandwidth limit = BandwidthBuilder.builder()
                    .capacity(publicDailyLimit)
                    .refillIntervally(publicDailyLimit,RESET_DURATION)
                    .build();
            Bucket bucket = Bucket.builder()
                    .addLimit(limit)
                    .build();
            globalPublicBucket.set(bucket);
        }

        public int getGlobalDailyLimit() {
            return publicDailyLimit;
        }

        // daily midnight reset
        @Scheduled(cron = "0 0 0 * * ?")
        public void scheduledReset() {
            resetGlobalBucket();
        }
    }
}

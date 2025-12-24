package org.voyager.api.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.Bucket;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class GlobalRateLimitService {
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
package org.voyager.api;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.Bucket;
import java.time.Duration;

public class test {
    public static void main(String[] args) {
        System.out.println("Testing Bucket4j JDK17...");

        try {
            Bandwidth limit = BandwidthBuilder.builder().capacity(10).refillGreedy(10,Duration.ofDays(1)).build();

            // Try Bucket4j.builder()
            try {
                Bucket bucket1 = Bucket.builder()
                        .addLimit(limit)
                        .build();
                System.out.println("✓ Bucket4j.builder() works");
            } catch (Exception e) {
                System.out.println("✗ Bucket4j.builder() failed: " + e.getMessage());
            }

            // Try Bucket.builder()
            try {
                Bucket bucket2 = Bucket.builder()
                        .addLimit(limit)
                        .build();
                System.out.println("✓ Bucket.builder() works");
            } catch (Exception e) {
                System.out.println("✗ Bucket.builder() failed: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

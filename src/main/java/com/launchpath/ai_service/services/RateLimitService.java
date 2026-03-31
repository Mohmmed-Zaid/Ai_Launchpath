package com.launchpath.ai_service.services;

import com.launchpath.ai_service.feign.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final UserServiceClient userServiceClient;

    @Value("${rate.limit.requests-per-minute:10}")
    private int requestsPerMinute;

    // In-memory — swap for Redis in Phase 3B
    private final Map<String, AtomicInteger> requestCounts =
            new ConcurrentHashMap<>();

    public boolean isAllowed(String userId) {
        if (userId == null) return false;

        if (isPaidUser(userId)) {
            log.debug("Paid user {} - unlimited access", userId);
            return true;
        }

        // Free user — enforce per-minute limit
        AtomicInteger count = requestCounts.computeIfAbsent(
                userId, k -> new AtomicInteger(0)
        );
        int current = count.incrementAndGet();

        if (current > requestsPerMinute) {
            log.warn("Rate limit exceeded for userId: {}", userId);
            return false;
        }

        log.debug("User {} - request {}/{}", userId, current, requestsPerMinute);
        return true;
    }

    public long getRemainingTokens(String userId) {
        AtomicInteger count = requestCounts.get(userId);
        if (count == null) return requestsPerMinute;
        return Math.max(0, requestsPerMinute - count.get());
    }

    // Resets every minute
    @Scheduled(fixedRate = 60000)
    public void resetCounts() {
        requestCounts.clear();
        log.debug("Rate limit counts reset");
    }

    // Check via Feign if user is paid
    // Paid users get unlimited access
    private boolean isPaidUser(String userId) {
        try {
            Boolean isActive = userServiceClient
                    .isSubscriptionActive(userId)
                    .getData();

            Integer remaining = userServiceClient
                    .getRemainingAtsCredits(userId)
                    .getData();

            return Boolean.TRUE.equals(isActive)
                    && remaining != null
                    && remaining > 3;

        } catch (Exception e) {
            log.warn("Could not check plan for userId: {} " +
                    "— defaulting to free limits", userId);
            return false;
        }
    }
}

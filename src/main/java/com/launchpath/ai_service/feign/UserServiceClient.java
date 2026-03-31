package com.launchpath.ai_service.feign;

import com.launchpath.ai_service.feign.dto.ApiResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "user-service",
        fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    @GetMapping("/api/v1/subscriptions/my/active")
    ApiResponseDTO<Boolean> isSubscriptionActive(
            @RequestHeader("X-User-Id") String userId
    );

    @GetMapping("/api/v1/subscriptions/my/ats-credits")
    ApiResponseDTO<Integer> getRemainingAtsCredits(
            @RequestHeader("X-User-Id") String userId
    );

    @PostMapping("/api/v1/subscriptions/my/consume-ats")
    ApiResponseDTO<Void> consumeAtsCredit(
            @RequestHeader("X-User-Id") String userId
    );

    @PostMapping("/api/v1/subscriptions/my/refund-ats")
    ApiResponseDTO<Void> refundAtsCredit(
            @RequestHeader("X-User-Id") String userId
    );
}
package com.launchpath.ai_service.feign;

import com.launchpath.ai_service.feign.dto.ApiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ApiResponseDTO<Boolean> isSubscriptionActive(String userId) {
        log.warn("user-service unavailable — defaulting to active");
        ApiResponseDTO<Boolean> response = new ApiResponseDTO<>();
        response.setSuccess(true);
        response.setData(true);
        return response;
    }

    @Override
    public ApiResponseDTO<Integer> getRemainingAtsCredits(String userId) {
        log.warn("user-service unavailable — defaulting to free tier");
        ApiResponseDTO<Integer> response = new ApiResponseDTO<>();
        response.setSuccess(true);
        response.setData(0); // 0 remaining = free tier
        return response;
    }

    @Override
    public ApiResponseDTO<Void> consumeAtsCredit(String userId) {
        log.warn("user-service unavailable — credit consume skipped");
        ApiResponseDTO<Void> response = new ApiResponseDTO<>();
        response.setSuccess(false);
        response.setMessage("user-service unavailable");
        return response;
    }

    @Override
    public ApiResponseDTO<Void> refundAtsCredit(String userId) {
        log.warn("user-service unavailable — credit refund skipped");
        ApiResponseDTO<Void> response = new ApiResponseDTO<>();
        response.setSuccess(false);
        response.setMessage("user-service unavailable");
        return response;
    }
}
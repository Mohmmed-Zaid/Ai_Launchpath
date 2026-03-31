package com.launchpath.ai_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.launchpath.ai_service.feign")
@EnableJpaAuditing
@EnableScheduling
public class AiServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(AiServiceApplication.class, args);
	}
}
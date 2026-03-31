// SHA-256 resume hashing

package com.launchpath.ai_service.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HashingService {

    /**
     * Generates SHA-256 hash from resume content string.
     * Called before any AI analysis to check cache.
     * If hash unchanged → same resume → return cached result.
     */
    public String hashResumeContent(String resumeContent) {
        if (resumeContent == null || resumeContent.isBlank()) {
            throw new IllegalArgumentException(
                    "Resume content cannot be empty for hashing"
            );
        }
        String hash = DigestUtils.sha256Hex(
                resumeContent.trim().toLowerCase()
        );
        log.debug("Generated hash: {}...", hash.substring(0, 8));
        return hash;
    }

    public String hashFromParts(java.util.List<String> parts) {
        String combined = String.join("|", parts);
        return hashResumeContent(combined);
    }
}
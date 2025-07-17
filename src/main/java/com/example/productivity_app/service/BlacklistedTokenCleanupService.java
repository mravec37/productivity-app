package com.example.productivity_app.service;

import com.example.productivity_app.controller.TaskController;
import com.example.productivity_app.entity.BlacklistedToken;
import com.example.productivity_app.repository.BlacklistedTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service responsible for cleaning up expired JWT tokens
 * from the blacklist to prevent database bloat.
 */
@Service
public class BlacklistedTokenCleanupService {

    private final BlacklistedTokenRepository tokenBlacklistRepository;
    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(BlacklistedTokenCleanupService.class);

    public BlacklistedTokenCleanupService(BlacklistedTokenRepository tokenBlacklistRepository, JwtService jwtService) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.jwtService = jwtService;
    }

    /**
     * Scheduled task that runs every hour to remove expired tokens
     * from the blacklist database.
     */
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanupExpiredBlacklistedTokens() {
        List<BlacklistedToken> blacklistedTokens = tokenBlacklistRepository.findAll();

        for (BlacklistedToken tokenEntity : blacklistedTokens) {
            String token = tokenEntity.getToken();
            if (jwtService.isTokenExpired(token)) {
                tokenBlacklistRepository.delete(tokenEntity);
                logger.debug("Expired blacklisted token {} deleted from database", token);
            }
        }
    }
}


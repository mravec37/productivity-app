package com.example.productivity_app.service;

import com.example.productivity_app.entity.BlacklistedToken;
import com.example.productivity_app.repository.BlacklistedTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BlacklistedTokenCleanupService {

    private final BlacklistedTokenRepository tokenBlacklistRepository;
    private final JwtService jwtService;

    public BlacklistedTokenCleanupService(BlacklistedTokenRepository tokenBlacklistRepository, JwtService jwtService) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.jwtService = jwtService;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanupExpiredBlacklistedTokens() {
        List<BlacklistedToken> blacklistedTokens = tokenBlacklistRepository.findAll();

        for (BlacklistedToken tokenEntity : blacklistedTokens) {
            String token = tokenEntity.getToken();
            if (jwtService.isTokenExpired(token)) {
                tokenBlacklistRepository.delete(tokenEntity);
            }
        }
    }
}


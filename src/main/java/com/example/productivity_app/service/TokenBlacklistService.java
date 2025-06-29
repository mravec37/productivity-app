package com.example.productivity_app.service;

import com.example.productivity_app.entity.BlacklistedToken;
import com.example.productivity_app.repository.BlacklistedTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final BlacklistedTokenRepository repository;

    public TokenBlacklistService(BlacklistedTokenRepository repository) {
        this.repository = repository;
    }

    public void blacklistToken(String token) {
        if (!repository.findByToken(token).isPresent()) {
            repository.save(new BlacklistedToken(token));
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return repository.findByToken(token).isPresent();
    }
}

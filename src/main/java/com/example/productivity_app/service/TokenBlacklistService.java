package com.example.productivity_app.service;

import com.example.productivity_app.entity.BlacklistedToken;
import com.example.productivity_app.repository.BlacklistedTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service to manage blacklisting of JWT tokens.
 * Allows adding tokens to the blacklist and checking if a token is blacklisted.
 */
@Service
public class TokenBlacklistService {

    private final BlacklistedTokenRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    public TokenBlacklistService(BlacklistedTokenRepository repository) {
        this.repository = repository;
    }

    private void blacklistToken(String token) {
        if (repository.findByToken(token).isEmpty()) {
            repository.save(new BlacklistedToken(token));
            logger.debug("Token blacklisted");
        }
    }

    /**
     * Extracts the token from the HTTP Authorization header and blacklists it.
     */
    public void blacklistTokenByHTTPRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        //Bearer indicates authentication via token
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            blacklistToken(token);
        } else {
            logger.warn("No token provided to blacklist");
        }
    }


    public boolean isTokenBlacklisted(String token) {
        return repository.findByToken(token).isPresent();
    }
}

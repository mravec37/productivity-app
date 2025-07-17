package com.example.productivity_app.service;

import com.example.productivity_app.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for generating, validating, and extracting information from JWT tokens.
 */
@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    @Autowired
    private UserDetailsService userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    /**
     * Extracts the username (subject) from the JWT token.
     */
   public String extractUsername(String token) {
       try {
           Claims claims = getAllClaimsFromToken(token);
           return claims.getSubject();
       } catch (Exception e) {
           logger.error("Failed to extract username, error: {} ", e.getMessage());
           e.printStackTrace();
           return null;
       }
   }

    /**
     * Parses the token and returns all claims (token data) inside it.
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build();
            return parser.parseClaimsJws(token).getBody();
        } catch (Exception e) {
            logger.error("Token parsing failed: {} ", e.getMessage());
            throw e;
        }
    }


    public String generateRefreshToken(UserDetails userDetails) {
        Instant now = Instant.now();
        Instant expirationInstant = now.plus(Duration.ofDays(7));

        return Jwts.builder()
                .setSubject(((User) userDetails).getEmail())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expirationInstant))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }



    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        logger.debug("Claims extracted");
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(Map.of(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }


    /**
     * Builds the JWT token with claims, subject, issued time, expiration, and signing.
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(((User) userDetails).getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the token by checking username and expiration.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        UserDetails user = this.userDetailsService.loadUserByUsername(username);
        return (user.getUsername().equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            logger.warn("Token is expired");
            return true;
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Returns the signing key used to sign and verify tokens.
     */
    private Key getSignInKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

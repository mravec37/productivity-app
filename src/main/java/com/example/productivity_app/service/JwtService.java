package com.example.productivity_app.service;

import com.example.productivity_app.entity.User;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
//import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

   /* public String extractUsername(String token) {
        System.out.println("Extract Username method");
        System.out.println("Token is: " + token);
        return extractClaim(token, Claims::getSubject);
    }*/
    @Autowired
    private UserDetailsService userDetailsService;
   public String extractUsername(String token) {
       System.out.println("Extract Username method");
       System.out.println("Token is: " + token);
       try {
           Claims claims = getAllClaimsFromToken(token);
           return claims.getSubject(); // usually the username
       } catch (Exception e) {
           System.err.println("Failed to extract username: " + e.getMessage());
           e.printStackTrace();
           System.out.println("Are you there?");
           return null;
       }
   }

    private Claims getAllClaimsFromToken(String token) {
        try {
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build();
            System.out.println("Jwt parser built");
            return parser.parseClaimsJws(token).getBody();
        } catch (Exception e) {
            System.err.println("Token parsing failed: " + e.getMessage());
            throw e; // rethrow so extractUsername logs it too
        }
    }

    public String generateRefreshToken(UserDetails userDetails) {
        /*Instant now = Instant.now();
        Instant expirationInstant = now.plus(7, ChronoUnit.DAYS);*/
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
        System.out.println("Going to extract claims");
        final Claims claims = extractAllClaims(token);
        System.out.println("Claims extracted");
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(Map.of(), userDetails);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(((User) userDetails).getEmail()) // NEW
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token); //extrahne sa email
        UserDetails user = this.userDetailsService.loadUserByUsername(username); //extrahneme usera vdaka emailu(username je email ked pouzivame userDetServ)
        return (user.getUsername().equals(userDetails.getUsername()) && !isTokenExpired(token)); //porovna sa email s username
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        //Tu je chyba
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

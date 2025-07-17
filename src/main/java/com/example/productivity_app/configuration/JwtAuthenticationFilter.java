package com.example.productivity_app.configuration;

import com.example.productivity_app.service.JwtService;
import com.example.productivity_app.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter responsible for JWT authentication — extracts username from token, verifies user existence,token validity,
 * and ensures the token was not manipulated or blacklisted. It runs once per HTTP request.
 */

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(
            HandlerExceptionResolver handlerExceptionResolver,
            JwtService jwtService,
            UserDetailsService userDetailsService,
            TokenBlacklistService tokenBlacklistService) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        logger.debug("Processing JWT authentication for {} {}", request.getMethod(), request.getRequestURI());
        final String authHeader = request.getHeader("Authorization");

        //If there was no token provided in header, we skip JWT authentication
        if (isAuthHeaderMissingOrInvalid(authHeader)) {
            logger.debug("No Authorization header found or header is invalid. Skipping JWT authentication.");
            filterChain.doFilter(request, response);
            return;
        }
        try {
            final String jwt = extractJwtFromHeader(authHeader);
            logger.debug("Authorization header present. JWT token begins with: {}", jwt.substring(0, Math.min(10, jwt.length())));

            //If the token was blacklisted- user logged off via that token, the authentication fails
            if (isTokenBlacklisted(jwt)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            final String username = jwtService.extractUsername(jwt);

            //Check if the authentication object already exists in security context, if yes, don't authenticate via token
            if (shouldAuthenticate(username)) {
                authenticateUser(request, jwt, username);
            }

            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            logger.error("Exception occurred during JWT authentication", exception);
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }

    private boolean isTokenBlacklisted(String jwt) {
        if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
            logger.warn("JWT token is blacklisted. Rejecting request.");
            return true;
        }
        return false;
    }

    private boolean isAuthHeaderMissingOrInvalid(String authHeader) {
        return authHeader == null || !authHeader.startsWith("Bearer ");
    }

    private String extractJwtFromHeader(String authHeader) {
        return authHeader.substring(7);
    }

    private boolean shouldAuthenticate(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return username != null &&
                (authentication == null || !authentication.isAuthenticated()
                        || authentication instanceof AnonymousAuthenticationToken);
    }

    private void authenticateUser(HttpServletRequest request, String jwt, String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        logger.debug("Loaded user details for username: {}", userDetails.getUsername());

        if (!jwtService.isTokenValid(jwt, userDetails)) {
            logger.warn("JWT validation failed for user: {}", username);
            return;
        }
        logger.info("JWT validated successfully for user: {}", username);
        setAuthentication(request, userDetails);
    }
    private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}

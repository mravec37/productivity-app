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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final HandlerExceptionResolver handlerExceptionResolver;

    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(HandlerExceptionResolver handlerExceptionResolver, JwtService jwtService,
                                   UserDetailsService userDetailsService, TokenBlacklistService tokenBlacklistService) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("Accessing JwtFilter");

        final String authHeader = request.getHeader("Authorization");
        if (isAuthHeaderMissingOrInvalid(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = extractJwtFromHeader(authHeader);
            System.out.println("Auth header: " + authHeader);

            if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            final String username = jwtService.extractUsername(jwt);
            if (shouldAuthenticate(username)) {
                authenticateUser(request, jwt, username);
            }

            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            exception.printStackTrace();
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
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
        System.out.println("User details true username: " + userDetails.getUsername());

        if (jwtService.isTokenValid(jwt, userDetails)) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

}

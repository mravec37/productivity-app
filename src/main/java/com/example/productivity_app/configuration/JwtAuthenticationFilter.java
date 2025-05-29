package com.example.productivity_app.configuration;

import com.example.productivity_app.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    public JwtAuthenticationFilter(HandlerExceptionResolver handlerExceptionResolver, JwtService jwtService, UserDetailsService userDetailsService) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("Accessing JwtFilter");
        final String authHeader = request.getHeader("Authorization");
        System.out.println("Jwt Filter PlaceHolder 1");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Jwt Filter PlaceHolder 2");
            filterChain.doFilter(request, response);
            System.out.println("Jwt Filter PlaceHolder 3");
            return;
        }

        try {
            System.out.println("Jwt Filter PlaceHolder 4");
            System.out.println("Auth header: " + authHeader);
            final String jwt = authHeader.substring(7);
            System.out.println("Jwt Filter PlaceHolder 4.5");
            final String username = jwtService.extractUsername(jwt);
            System.out.println("Username is: " + username);
            System.out.println("Jwt Filter PlaceHolder 5");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            //System.out.println("Authentication: " + authentication.getPrincipal() + ":" + authentication.getAuthorities());

            if (username != null && (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken)) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                System.out.println("User details true username: " + userDetails.getUsername());
                System.out.println("Jwt Filter PlaceHolder 6");
                System.out.println("Is token valid: " + jwtService.isTokenValid(jwt, userDetails));

                if(jwtService.isTokenValid(jwt, userDetails)) {
                    System.out.println("Jwt Filter PlaceHolder 7");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                            null, userDetails.getAuthorities());
                    authToken.setDetails((new WebAuthenticationDetailsSource().buildDetails(request)));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("Jwt Filter PlaceHolder 8");
                }
            }
            System.out.println("Jwt Filter PlaceHolder 9");
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            System.out.println("Filter Exception");
            exception.printStackTrace();
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }
}

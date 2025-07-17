package com.example.productivity_app.controller;

import com.example.productivity_app.dto.user_authentication.LoginResponse;
import com.example.productivity_app.dto.user_authentication.LoginUserDto;
import com.example.productivity_app.dto.user_authentication.RegisterUserDto;
import com.example.productivity_app.dto.user_authentication.VerifyUserDto;
import com.example.productivity_app.entity.User;
import com.example.productivity_app.service.AuthenticationService;
import com.example.productivity_app.service.JwtService;
import com.example.productivity_app.service.TokenBlacklistService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * Controller handling user authentication:
 * - Sign up, login, email verification
 * - JWT token handling
 * - Logout and token blacklist
 *
 * Exposes REST endpoints under /auth.
 */
@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    @Autowired
    private UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    private static Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService,
                                    TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto) {
        try {
            User registeredUser = authenticationService.signup(registerUserDto);
            logger.info("New user: {} registered",registeredUser.getUsername());
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Used for user login, if the authentication is successful, JWT access token is returned.
     * Sets a refresh token as an HTTP-only cookie in the response headers.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto, HttpServletResponse response) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String accessToken = jwtService.generateToken(authenticatedUser);
        String refreshToken = jwtService.generateRefreshToken(authenticatedUser);

        ResponseCookie cookie = buildCookie(refreshToken, 7 * 24 * 60 * 60);
        response.addHeader("Set-Cookie", cookie.toString());
        logger.info("User: {} signed in",authenticatedUser.getUsername());

        return ResponseEntity.ok(new LoginResponse(accessToken));
    }

    private static ResponseCookie buildCookie(String refreshToken, int maxAgeSeconds) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // use false if you're testing over HTTP
                .path("/")
                .maxAge(maxAgeSeconds) // 7 days
                .sameSite("Strict")
                .build();
    }

    /**
     * Returns new access token if user has valid refresh token.
     * Looks for the refresh token in an HTTP cookie named "refreshToken."
     * If valid, generates and returns a new JWT access token.
     * @param request the HTTP request containing cookies
     * @return 200 OK with new JWT access token, or 401 if refresh token is missing or invalid
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken == null) {
            logger.warn("Refresh token is missing");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token missing");
        }

        String username = jwtService.extractUsername(refreshToken);
        User userDetails = (User) userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            logger.warn("Invalid refresh token: {}", refreshToken);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token invalid");
        }

        String newAccessToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponse(newAccessToken));
    }

    /**
     * Used for user to logout, it blacklists the current access token so it can no longer be used.
     * Clears the refresh token cookie by overwriting it with an empty value and zero max age.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        tokenBlacklistService.blacklistTokenByHTTPRequest(request);

        ResponseCookie cookie = buildCookie("", 0);
        response.setHeader("Set-Cookie", cookie.toString());
        String username = getAuthenticationUsername();

        logger.info("User: {} is logging out", username);

        return ResponseEntity.ok("Logged out successfully.");
    }

    private static String getAuthenticationUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = "Anonymous";

        if (authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {
            username = authentication.getName();
        }
        return username;
    }


    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        try {
            authenticationService.verifyUser(verifyUserDto);
            logger.info("Email: {} verified successfully", verifyUserDto.getEmail());
            return ResponseEntity.ok("Account verified successfully");
        } catch(RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
    }
}

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        try {
            authenticationService.resendVerificationCode(email);
            logger.info("Verification code for email: {} resent", email);
            return ResponseEntity.ok("Verification code sent");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

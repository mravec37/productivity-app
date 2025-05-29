package com.example.productivity_app.controller;

import com.example.productivity_app.dto.user_authentication.LoginResponse;
import com.example.productivity_app.dto.user_authentication.LoginUserDto;
import com.example.productivity_app.dto.user_authentication.RegisterUserDto;
import com.example.productivity_app.dto.user_authentication.VerifyUserDto;
import com.example.productivity_app.entity.User;
import com.example.productivity_app.service.AuthenticationService;
import com.example.productivity_app.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.CachingUserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RequestMapping("/auth")
@RestController
@CrossOrigin(origins = "http://localhost")
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    @Autowired
    private UserDetailsService userDetailsService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto) {
        try {
            System.out.println("Going to signup a user");
            User registeredUser = authenticationService.signup(registerUserDto);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto, HttpServletResponse response) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String accessToken = jwtService.generateToken(authenticatedUser);
        String refreshToken = jwtService.generateRefreshToken(authenticatedUser);

        // Send refresh token in HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // use false if you're testing over HTTP
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(new LoginResponse(accessToken));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        System.out.println("Call to refresh token");
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken == null) {
            System.out.println("Refresh token missing");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token missing");
        }

        System.out.println("Refresh token: " + refreshToken);

        String username = jwtService.extractUsername(refreshToken);
        User userDetails = (User) userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            System.out.println("Refresh token invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token invalid");
        }

        String newAccessToken = jwtService.generateToken(userDetails);

        System.out.println("New access token: " + newAccessToken);

        return ResponseEntity.ok(new LoginResponse(newAccessToken));
    }



    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        try {
            authenticationService.verifyUser(verifyUserDto);
            return ResponseEntity.ok("Account verified successfully");
        } catch(RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
    }
}

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        try {
            authenticationService.resendVerificationCode(email);
            return ResponseEntity.ok("Verification code sent");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

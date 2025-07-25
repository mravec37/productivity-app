package com.example.productivity_app.controller;

import com.example.productivity_app.dto.user_authentication.RegisterUserDto;
import com.example.productivity_app.dto.user_authentication.VerifyUserDto;
import com.example.productivity_app.entity.User;
import com.example.productivity_app.service.AuthenticationService;
import com.example.productivity_app.service.JwtService;
import com.example.productivity_app.service.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterUserDto dto = new RegisterUserDto();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("secret");

        User mockUser = new User("testuser", "test@example.com", "encodedSecret");
        mockUser.setId(1L);
        mockUser.setEnabled(false);
        mockUser.setVerificationCode("123456");
        mockUser.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(mockUser);

        String requestJson = """
                {
                  "username": "testuser",
                  "email": "test@example.com",
                  "password": "secret"
                }
                """;

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void shouldVerifyUserSuccessfully() throws Exception {
        VerifyUserDto dto = new VerifyUserDto();
        dto.setEmail("test@example.com");
        dto.setVerificationCode("123456");

        doNothing().when(authenticationService).verifyUser(any(VerifyUserDto.class));

        mockMvc.perform(post("/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Account verified successfully"));
    }
}
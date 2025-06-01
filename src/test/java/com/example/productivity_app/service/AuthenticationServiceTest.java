package com.example.productivity_app.service;

import com.example.productivity_app.dto.user_authentication.RegisterUserDto;
import com.example.productivity_app.entity.User;
import com.example.productivity_app.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldCreateDisabledUserWithVerificationCodeAndEncryptedPassword() throws MessagingException {

        RegisterUserDto dto = new RegisterUserDto();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("heslo123");

        String encodedPassword = "encoded-password";
        when(passwordEncoder.encode("heslo123")).thenReturn(encodedPassword);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = authenticationService.signup(dto);

        User capturedUser = userCaptor.getValue();

        assertThat(savedUser).isNotNull();
        assertThat(capturedUser.getUsername()).isEqualTo("testuser");
        assertThat(capturedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(capturedUser.isEnabled()).isFalse();
        assertThat(capturedUser.getVerificationCode()).isNotNull();
        assertThat(capturedUser.getVerificationCodeExpiresAt()).isAfter(LocalDateTime.now().minusSeconds(1));
        assertThat(capturedUser.getPassword()).isEqualTo(encodedPassword);

        verify(passwordEncoder).encode("heslo123");
        verify(emailService).sendVerificationEmail(any(), any(), any());
        verify(userRepository).save(any(User.class));
    }
}

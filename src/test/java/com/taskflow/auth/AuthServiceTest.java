package com.taskflow.auth;

import com.taskflow.auth.dto.request.LoginRequest;
import com.taskflow.auth.dto.request.RegisterRequest;
import com.taskflow.auth.dto.response.AuthResponse;
import com.taskflow.auth.entity.RefreshToken;
import com.taskflow.auth.repository.RefreshTokenRepository;
import com.taskflow.auth.service.AuthService;
import com.taskflow.auth.service.JwtProperties;
import com.taskflow.auth.service.JwtService;
import com.taskflow.auth.service.OtpService;
import com.taskflow.common.exception.AuthenticationException;
import com.taskflow.common.exception.ConflictException;
import com.taskflow.common.exception.ValidationException;
import com.taskflow.user.entity.User;
import com.taskflow.user.entity.UserRole;
import com.taskflow.user.entity.UserStatus;
import com.taskflow.user.mapper.UserMapper;
import com.taskflow.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;
    @Mock private OtpService otpService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProperties jwtProperties;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_whenEmailAlreadyExists_throwsConflictException() {
        RegisterRequest request = mockRegisterRequest("existing@example.com");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("existing@example.com");
    }

    @Test
    void register_withNewEmail_savesUserAndSendsOtp() {
        RegisterRequest request = mockRegisterRequest("new@example.com");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");
        when(userRepository.save(any())).thenReturn(new User());

        authService.register(request);

        verify(userRepository).save(any(User.class));
        verify(otpService).generateAndSendOtp("new@example.com");
    }

    @Test
    void login_withInvalidPassword_throwsAuthenticationException() {
        LoginRequest request = mockLoginRequest("user@example.com", "wrong");
        User user = buildUser("user@example.com", "encoded", true, UserStatus.ACTIVE);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void login_withUnverifiedEmail_throwsValidationException() {
        LoginRequest request = mockLoginRequest("user@example.com", "pass");
        User user = buildUser("user@example.com", "encoded", false, UserStatus.ACTIVE);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "encoded")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("verify your email");
    }

    @Test
    void login_withValidCredentials_returnsAuthResponse() {
        LoginRequest request = mockLoginRequest("user@example.com", "pass");
        User user = buildUser("user@example.com", "encoded", true, UserStatus.ACTIVE);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "encoded")).thenReturn(true);
        when(jwtService.generateAccessToken(any())).thenReturn("access_token");
        when(refreshTokenRepository.save(any())).thenReturn(new RefreshToken());
        when(jwtProperties.getAccessTokenExpiryMs()).thenReturn(900000L);
        when(jwtProperties.getRefreshTokenExpiryMs()).thenReturn(604800000L);
        when(userMapper.toResponse(any())).thenReturn(null);

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    private RegisterRequest mockRegisterRequest(String email) {
        RegisterRequest req = mock(RegisterRequest.class);
        when(req.getEmail()).thenReturn(email);
        when(req.getFirstName()).thenReturn("Test");
        when(req.getLastName()).thenReturn("User");
        when(req.getPassword()).thenReturn("password123");
        return req;
    }

    private LoginRequest mockLoginRequest(String email, String password) {
        LoginRequest req = mock(LoginRequest.class);
        when(req.getEmail()).thenReturn(email);
        when(req.getPassword()).thenReturn(password);
        return req;
    }

    private User buildUser(String email, String encodedPassword, boolean verified, UserStatus status) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setEmailVerified(verified);
        user.setStatus(status);
        user.setRole(UserRole.USER);
        return user;
    }
}

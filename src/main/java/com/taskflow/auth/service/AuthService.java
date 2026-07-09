package com.taskflow.auth.service;

import com.taskflow.auth.dto.request.*;
import com.taskflow.auth.dto.response.AuthResponse;
import com.taskflow.auth.entity.RefreshToken;
import com.taskflow.auth.repository.RefreshTokenRepository;
import com.taskflow.common.exception.AuthenticationException;
import com.taskflow.common.exception.ConflictException;
import com.taskflow.common.exception.ResourceNotFoundException;
import com.taskflow.common.exception.ValidationException;
import com.taskflow.user.dto.response.UserResponse;
import com.taskflow.user.entity.User;
import com.taskflow.user.entity.UserRole;
import com.taskflow.user.entity.UserStatus;
import com.taskflow.user.mapper.UserMapper;
import com.taskflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    @Transactional
    public void register(RegisterRequest request) {
        log.debug("Registering new user: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("An account with email '" + request.getEmail() + "' already exists");
        }
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
        userRepository.save(user);
        otpService.generateAndSendOtp(request.getEmail());
        log.info("User registered successfully: {}", request.getEmail());
    }

    @Transactional
    public AuthResponse verifyEmail(VerifyOtpRequest request) {
        log.debug("Verifying email for: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));
        if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            throw new ValidationException("Invalid or expired OTP");
        }
        user.setEmailVerified(true);
        userRepository.save(user);
        otpService.invalidateOtp(request.getEmail());
        log.info("Email verified for user: {}", request.getEmail());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt for: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }
        if (!user.isEmailVerified()) {
            throw new ValidationException("Please verify your email before logging in");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationException("Your account is " + user.getStatus().name().toLowerCase() + ". Please contact support.");
        }
        log.info("User logged in: {}", request.getEmail());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));
        if (refreshToken.isRevoked()) {
            throw new AuthenticationException("Refresh token has been revoked");
        }
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("Refresh token has expired. Please log in again.");
        }
        User user = refreshToken.getUser();
        UserDetails userDetails = buildUserDetails(user);
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .accessTokenExpiresIn(jwtProperties.getAccessTokenExpiryMs())
                .user(userMapper.toResponse(user))
                .build();
    }

    @Transactional
    public void logout(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.debug("Refresh token revoked for user: {}", token.getUser().getEmail());
        });
    }

    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));
        if (user.isEmailVerified()) {
            throw new ValidationException("Email is already verified");
        }
        otpService.generateAndSendOtp(request.getEmail());
        log.debug("OTP resent for: {}", request.getEmail());
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = buildUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshTokenValue = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshTokenExpiryMs() / 1000);
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .accessTokenExpiresIn(jwtProperties.getAccessTokenExpiryMs())
                .user(userMapper.toResponse(user))
                .build();
    }

    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}

package com.taskflow.auth;

import com.taskflow.auth.service.JwtProperties;
import com.taskflow.auth.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtProperties jwtProperties;

    private JwtService jwtService;

    private static final String SECRET =
            "dGVzdFNlY3JldEtleUZvclRhc2tGbG93QXBwbGljYXRpb25UZXN0aW5nMTIzNDU2Nzg=";

    @BeforeEach
    void setUp() {
        when(jwtProperties.getSecret()).thenReturn(SECRET);
        when(jwtProperties.getAccessTokenExpiryMs()).thenReturn(900000L);
        jwtService = new JwtService(jwtProperties);
    }

    @Test
    void generateAccessToken_returnsNonNullToken() {
        UserDetails userDetails = buildUserDetails("test@example.com");
        String token = jwtService.generateAccessToken(userDetails);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String email = "test@example.com";
        UserDetails userDetails = buildUserDetails(email);
        String token = jwtService.generateAccessToken(userDetails);
        assertThat(jwtService.extractEmail(token)).isEqualTo(email);
    }

    @Test
    void isTokenValid_withMatchingUser_returnsTrue() {
        UserDetails userDetails = buildUserDetails("valid@example.com");
        String token = jwtService.generateAccessToken(userDetails);
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_withDifferentUser_returnsFalse() {
        UserDetails tokenOwner = buildUserDetails("owner@example.com");
        UserDetails otherUser = buildUserDetails("other@example.com");
        String token = jwtService.generateAccessToken(tokenOwner);
        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    private UserDetails buildUserDetails(String email) {
        return new User(email, "password", Collections.emptyList());
    }
}

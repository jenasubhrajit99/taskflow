package com.taskflow.auth.dto.response;

import com.taskflow.user.dto.response.UserResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private final String accessToken;
    private final String refreshToken;
    @Builder.Default
    private final String tokenType = "Bearer";
    private final long accessTokenExpiresIn;
    private final UserResponse user;
}

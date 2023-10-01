package com.anto.authservice.model.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenRefreshResponse {
    private final String type = "Bearer";
    private String accessToken;
    private String refreshToken;
}
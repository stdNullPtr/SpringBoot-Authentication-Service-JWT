package com.anto.authservice.model.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TokenRefreshRequest {
    @NotBlank
    @Size(min = 36, max = 36)
    private String refreshToken;
}
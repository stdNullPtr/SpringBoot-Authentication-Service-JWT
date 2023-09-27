package com.xaxo.authservice.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ERole {
    ROLE_USER("user"),
    ROLE_MODERATOR("mod"),
    ROLE_ADMIN("admin");

    private final String roleString;

    public static ERole fromString(String role) {
        for (ERole eRole : ERole.values()) {
            if (eRole.getRoleString().equalsIgnoreCase(role)) {
                return eRole;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + role);
    }
}
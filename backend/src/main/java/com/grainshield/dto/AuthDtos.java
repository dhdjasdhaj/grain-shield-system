package com.grainshield.dto;

import com.grainshield.enums.Role;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record UserProfile(Long id, String username, String realName, Role role, List<Long> managedNodeIds) {}
    public record LoginResponse(String token, UserProfile user) {}
}

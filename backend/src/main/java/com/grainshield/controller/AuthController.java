package com.grainshield.controller;

import com.grainshield.dto.ApiResponse;
import com.grainshield.dto.AuthDtos.*;
import com.grainshield.entity.SysUser;
import com.grainshield.repository.SysUserRepository;
import com.grainshield.security.JwtService;
import com.grainshield.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SysUserRepository userRepository;
    private final CurrentUserService currentUserService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        SysUser user = userRepository.findByUsername(request.username()).orElseThrow();
        var principal = User.withUsername(user.getUsername()).password(user.getPasswordHash())
                .roles(user.getRole().name()).build();
        String token = jwtService.generateToken(principal, Map.of("role", user.getRole().name(), "uid", user.getId()));
        return ApiResponse.ok("登录成功", new LoginResponse(token, profile(user)));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfile> me() {
        return ApiResponse.ok(profile(currentUserService.requireCurrentUser()));
    }

    private UserProfile profile(SysUser user) {
        return new UserProfile(user.getId(), user.getUsername(), user.getRealName(), user.getRole(),
                user.getManagedNodes().stream().map(n -> n.getId()).sorted().toList());
    }
}

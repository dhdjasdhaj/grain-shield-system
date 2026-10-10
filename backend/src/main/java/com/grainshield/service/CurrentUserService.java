package com.grainshield.service;

import com.grainshield.entity.SysUser;
import com.grainshield.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final SysUserRepository userRepository;

    public SysUser requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new IllegalStateException("未登录");
        return userRepository.findByUsername(auth.getName()).orElseThrow(() -> new IllegalStateException("用户不存在"));
    }

    public String username() {
        return requireCurrentUser().getUsername();
    }
}

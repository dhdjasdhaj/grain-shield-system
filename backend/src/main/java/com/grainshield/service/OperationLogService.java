package com.grainshield.service;

import com.grainshield.entity.OperationLog;
import com.grainshield.entity.SysUser;
import com.grainshield.repository.OperationLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OperationLogService {
    private final OperationLogRepository repository;
    private final CurrentUserService currentUserService;
    private final HttpServletRequest request;

    public void log(String module, String action, String content) {
        OperationLog log = new OperationLog();
        try {
            SysUser user = currentUserService.requireCurrentUser();
            log.setUserId(user.getId());
            log.setUsername(user.getUsername());
        } catch (Exception e) {
            log.setUsername("system");
        }
        log.setModule(module);
        log.setAction(action);
        log.setContent(content);
        try { log.setIpAddress(request.getRemoteAddr()); } catch (Exception ignored) { log.setIpAddress("local"); }
        log.setOperationTime(LocalDateTime.now());
        repository.save(log);
    }
}

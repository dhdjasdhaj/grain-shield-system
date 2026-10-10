package com.grainshield.service;

import com.grainshield.entity.SysUser;
import com.grainshield.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AccessService {
    public void requireNodeAccess(SysUser user, Long nodeId) {
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.DISPATCHER) return;
        boolean allowed = user.getManagedNodes().stream().anyMatch(n -> n.getId().equals(nodeId));
        if (!allowed) throw new AccessDeniedException("无权维护该节点数据");
    }
}

package com.grainshield.controller;

import com.grainshield.dto.ApiResponse;
import com.grainshield.dto.BusinessDtos.UserRequest;
import com.grainshield.dto.BusinessDtos.UserView;
import com.grainshield.entity.SysUser;
import com.grainshield.repository.SupplyNodeRepository;
import com.grainshield.repository.SysUserRepository;
import com.grainshield.service.OperationLogService;
import com.grainshield.service.ViewMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final SysUserRepository repository;
    private final SupplyNodeRepository nodeRepository;
    private final PasswordEncoder encoder;
    private final ViewMapper mapper;
    private final OperationLogService logService;

    @GetMapping public ApiResponse<List<UserView>> list(){return ApiResponse.ok(repository.findAll().stream().map(mapper::user).toList());}
    @PostMapping public ApiResponse<UserView> create(@Valid @RequestBody UserRequest r){if(repository.existsByUsername(r.username()))throw new IllegalArgumentException("用户名已存在");if(r.password()==null||r.password().length()<6)throw new IllegalArgumentException("初始密码至少6位");SysUser u=new SysUser();apply(u,r,true);u=repository.save(u);logService.log("用户权限","新增用户",u.getUsername());return ApiResponse.ok(mapper.user(u));}
    @PutMapping("/{id}") public ApiResponse<UserView> update(@PathVariable Long id,@Valid @RequestBody UserRequest r){SysUser u=repository.findById(id).orElseThrow(()->new EntityNotFoundException("用户不存在"));if(!u.getUsername().equals(r.username())&&repository.existsByUsername(r.username()))throw new IllegalArgumentException("用户名已存在");apply(u,r,false);u=repository.save(u);logService.log("用户权限","修改用户",u.getUsername());return ApiResponse.ok(mapper.user(u));}
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id){repository.deleteById(id);logService.log("用户权限","删除用户","ID="+id);return ApiResponse.ok("删除成功");}
    private void apply(SysUser u,UserRequest r,boolean create){u.setUsername(r.username());u.setRealName(r.realName());u.setRole(r.role());u.setEnabled(r.enabled()==null||r.enabled());if(create||r.password()!=null&&!r.password().isBlank())u.setPasswordHash(encoder.encode(r.password()));u.setManagedNodes(new HashSet<>(r.managedNodeIds()==null?List.of():nodeRepository.findAllById(r.managedNodeIds())));}
}

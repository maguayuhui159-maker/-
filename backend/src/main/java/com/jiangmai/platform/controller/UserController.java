package com.jiangmai.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangmai.platform.entity.User;
import com.jiangmai.platform.service.OperationLogService;
import com.jiangmai.platform.service.UserService;
import com.jiangmai.platform.utils.AuthUtils;
import com.jiangmai.platform.utils.OperationLogUtils;
import com.jiangmai.platform.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/users")
    public Result<Map<String, Object>> listUsers(@RequestParam(required = false, defaultValue = "1") Integer page,
                                                 @RequestParam(required = false, defaultValue = "10") Integer limit,
                                                 @RequestParam(required = false) String username,
                                                 @RequestParam(required = false) String role,
                                                 @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = requireAdmin(authorization);
        if (currentUser == null) {
            return Result.error("仅管理员可查看用户列表");
        }

        Page<User> pageParam = new Page<>(page, limit);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.and(w -> w.like("username", username).or().like("nickname", username));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq("role", role);
        }
        wrapper.orderByDesc("create_time");
        userService.page(pageParam, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("total", pageParam.getTotal());
        data.put("items", pageParam.getRecords());
        return Result.success(data);
    }

    @PutMapping("/users/{id}")
    public Result<String> updateUser(@PathVariable Long id,
                                     @RequestBody User payload,
                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = requireAdmin(authorization);
        if (currentUser == null) {
            return Result.error("仅管理员可编辑用户");
        }

        User existing = userService.getById(id);
        if (existing == null) {
            return Result.error("用户不存在");
        }
        if (StringUtils.hasText(payload.getNickname())) {
            existing.setNickname(payload.getNickname().trim());
        } else {
            existing.setNickname(null);
        }
        existing.setPhone(payload.getPhone());
        if (StringUtils.hasText(payload.getRole())) {
            String newRole = payload.getRole().trim().toUpperCase();
            if (!"ADMIN".equals(newRole) && !"STUDENT".equals(newRole) && !"UPLOADER".equals(newRole)) {
                return Result.error("角色无效");
            }
            existing.setRole(newRole);
        }
        userService.updateById(existing);
        OperationLogUtils.record(operationLogService, currentUser, "USER", "UPDATE", id, "编辑用户 " + existing.getUsername());
        return Result.success("更新成功");
    }

    @DeleteMapping("/users/{id}")
    public Result<String> deleteUser(@PathVariable Long id,
                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = requireAdmin(authorization);
        if (currentUser == null) {
            return Result.error("仅管理员可删除用户");
        }

        User existing = userService.getById(id);
        if (existing == null) {
            return Result.error("用户不存在");
        }
        if ("ADMIN".equals(existing.getRole())) {
            return Result.error("管理员账号不可删除");
        }
        userService.removeById(id);
        OperationLogUtils.record(operationLogService, currentUser, "USER", "DELETE", id, "删除用户 " + existing.getUsername());
        return Result.success("删除成功");
    }

    private User requireAdmin(String authorization) {
        Long userId = AuthUtils.parseUserId(authorization);
        if (userId == null) {
            return null;
        }
        User currentUser = userService.getById(userId);
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return null;
        }
        return currentUser;
    }
}

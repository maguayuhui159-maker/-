package com.jiangmai.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiangmai.platform.dto.ProfileChangeReviewDTO;
import com.jiangmai.platform.dto.ProfileChangeSubmitDTO;
import com.jiangmai.platform.entity.ProfileChangeRequest;
import com.jiangmai.platform.entity.User;
import com.jiangmai.platform.mapper.ProfileChangeRequestMapper;
import com.jiangmai.platform.mapper.UserMapper;
import com.jiangmai.platform.service.OperationLogService;
import com.jiangmai.platform.utils.AuthUtils;
import com.jiangmai.platform.utils.OperationLogUtils;
import com.jiangmai.platform.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
public class ProfileChangeController {

    @Autowired
    private ProfileChangeRequestMapper profileChangeRequestMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OperationLogService operationLogService;

    private User currentUser(String authorization) {
        Long userId = AuthUtils.parseUserId(authorization);
        return userId == null ? null : userMapper.selectById(userId);
    }

    @PostMapping("/api/profile-change/submit")
    public Result<String> submit(@RequestBody ProfileChangeSubmitDTO dto,
                                 @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }

        String requestedNickname = dto.getRequestedNickname() == null ? "" : dto.getRequestedNickname().trim();
        if (!StringUtils.hasText(requestedNickname)) {
            requestedNickname = dto.getRequestedUsername() == null ? "" : dto.getRequestedUsername().trim();
        }
        String requestedAvatar = dto.getRequestedAvatar() == null ? "" : dto.getRequestedAvatar().trim();
        if (!StringUtils.hasText(requestedNickname) && !StringUtils.hasText(requestedAvatar)) {
            return Result.error("请至少填写新昵称或选择新头像");
        }

        QueryWrapper<ProfileChangeRequest> pendingWrapper = new QueryWrapper<ProfileChangeRequest>()
                .eq("user_id", currentUser.getId())
                .eq("status", "PENDING")
                .last("limit 1");
        ProfileChangeRequest pending = profileChangeRequestMapper.selectOne(pendingWrapper);

        String finalRequestedNickname = StringUtils.hasText(requestedNickname)
                ? requestedNickname
                : (pending != null ? pending.getRequestedUsername() : "");
        String finalRequestedAvatar = StringUtils.hasText(requestedAvatar)
                ? requestedAvatar
                : (pending != null ? pending.getRequestedAvatar() : "");

        if (!StringUtils.hasText(finalRequestedNickname) && !StringUtils.hasText(finalRequestedAvatar)) {
            return Result.error("请至少填写新昵称或选择新头像");
        }

        if (pending != null) {
            pending.setCurrentUsername(currentUser.getUsername());
            pending.setRequestedUsername(finalRequestedNickname);
            pending.setRequestedAvatar(finalRequestedAvatar);
            pending.setUpdateTime(LocalDateTime.now());
            profileChangeRequestMapper.updateById(pending);
            OperationLogUtils.record(operationLogService, currentUser, "PROFILE", "UPDATE_REQUEST", pending.getId(), "更新资料修改申请");
            return Result.success("待审核申请已更新");
        }

        ProfileChangeRequest request = new ProfileChangeRequest();
        request.setUserId(currentUser.getId());
        request.setCurrentUsername(currentUser.getUsername());
        request.setRequestedUsername(finalRequestedNickname);
        request.setRequestedAvatar(finalRequestedAvatar);
        request.setStatus("PENDING");
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        profileChangeRequestMapper.insert(request);
        OperationLogUtils.record(operationLogService, currentUser, "PROFILE", "SUBMIT_REQUEST", request.getId(), "提交资料修改申请");
        return Result.success("申请已提交，等待管理员审核");
    }

    @GetMapping("/api/profile-change/my")
    public Result<List<ProfileChangeRequest>> myRequests(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.success(Collections.emptyList());
        }
        QueryWrapper<ProfileChangeRequest> wrapper = new QueryWrapper<ProfileChangeRequest>()
                .eq("user_id", currentUser.getId())
                .orderByDesc("create_time");
        return Result.success(profileChangeRequestMapper.selectList(wrapper));
    }

    @GetMapping("/api/profile-change/current")
    public Result<User> currentProfile(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        currentUser.setPassword(null);
        return Result.success(currentUser);
    }

    @GetMapping("/api/admin/profile-change-requests")
    public Result<List<ProfileChangeRequest>> adminList(@RequestParam(required = false) String status,
                                                        @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return Result.error("仅管理员可查看资料修改申请");
        }
        QueryWrapper<ProfileChangeRequest> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status.trim().toUpperCase());
        }
        wrapper.orderByDesc("create_time");
        return Result.success(profileChangeRequestMapper.selectList(wrapper));
    }

    @PutMapping("/api/admin/profile-change-requests/{id}/review")
    public Result<String> review(@PathVariable Long id,
                                 @RequestBody ProfileChangeReviewDTO dto,
                                 @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return Result.error("仅管理员可审核资料修改申请");
        }

        ProfileChangeRequest request = profileChangeRequestMapper.selectById(id);
        if (request == null) {
            return Result.error("申请记录不存在");
        }
        if (!"PENDING".equals(request.getStatus())) {
            return Result.error("该申请已处理");
        }

        String reviewStatus = dto.getStatus() == null ? "" : dto.getStatus().trim().toUpperCase();
        if (!"APPROVED".equals(reviewStatus) && !"REJECTED".equals(reviewStatus)) {
            return Result.error("审核状态非法");
        }

        User targetUser = userMapper.selectById(request.getUserId());
        if (targetUser == null) {
            return Result.error("申请用户不存在");
        }

        if ("APPROVED".equals(reviewStatus)) {
            String requestedNickname = request.getRequestedUsername() == null ? "" : request.getRequestedUsername().trim();
            if (StringUtils.hasText(requestedNickname)) {
                targetUser.setNickname(requestedNickname);
            }
            String requestedAvatar = request.getRequestedAvatar() == null ? "" : request.getRequestedAvatar().trim();
            if (StringUtils.hasText(requestedAvatar)) {
                targetUser.setAvatar(requestedAvatar);
            }
            userMapper.updateById(targetUser);
        }

        request.setStatus(reviewStatus);
        request.setReviewComment(dto.getReviewComment());
        request.setReviewedBy(currentUser.getUsername());
        request.setUpdateTime(LocalDateTime.now());
        profileChangeRequestMapper.updateById(request);
        OperationLogUtils.record(operationLogService, currentUser, "PROFILE", "REVIEW_REQUEST", id, "资料修改申请审核为 " + reviewStatus);
        return Result.success("审核完成");
    }
}

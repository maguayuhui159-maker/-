package com.jiangmai.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiangmai.platform.entity.OfflineActivity;
import com.jiangmai.platform.entity.OfflineBooking;
import com.jiangmai.platform.entity.User;
import com.jiangmai.platform.service.OfflineActivityService;
import com.jiangmai.platform.service.OfflineBookingService;
import com.jiangmai.platform.service.OperationLogService;
import com.jiangmai.platform.service.UserService;
import com.jiangmai.platform.utils.AuthUtils;
import com.jiangmai.platform.utils.OperationLogUtils;
import com.jiangmai.platform.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/offline")
public class OfflineController {

    @Autowired
    private OfflineActivityService offlineActivityService;

    @Autowired
    private OfflineBookingService offlineBookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/activities")
    public Result<List<OfflineActivity>> listActivities(@RequestParam(required = false) String status) {
        QueryWrapper<OfflineActivity> query = new QueryWrapper<>();
        if (status != null && !status.isBlank()) {
            query.eq("status", status.trim().toUpperCase());
        }
        query.orderByAsc("activity_time");
        return Result.success(offlineActivityService.list(query));
    }

    @PostMapping("/activities")
    public Result<String> createActivity(@RequestBody OfflineActivity payload,
                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        if (!"ADMIN".equals(currentUser.getRole())) {
            return Result.error("仅管理员可发布活动");
        }

        try {
            OfflineActivity activity = new OfflineActivity();
            activity.setTitle(payload.getTitle());
            activity.setCity(payload.getCity());
            activity.setLocation(payload.getLocation());
            activity.setActivityTime(payload.getActivityTime() == null ? LocalDateTime.now().plusDays(7) : payload.getActivityTime());
            activity.setQuota(payload.getQuota() == null ? 30 : Math.max(1, payload.getQuota()));
            activity.setBookedCount(0);
            activity.setPrice(payload.getPrice() == null ? BigDecimal.ZERO : payload.getPrice());
            activity.setStatus("OPEN");
            activity.setDescription(payload.getDescription());
            activity.setCreateTime(LocalDateTime.now());
            activity.setUpdateTime(LocalDateTime.now());
            offlineActivityService.save(activity);
            OperationLogUtils.record(operationLogService, currentUser, "OFFLINE", "CREATE_ACTIVITY", activity.getId(), "发布线下活动《" + activity.getTitle() + "》");
            return Result.success("活动已发布");
        } catch (Exception e) {
            return Result.error("活动发布失败");
        }
    }

    @PostMapping("/bookings")
    public Result<String> bookActivity(@RequestBody Map<String, Long> body,
                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        Long activityId = body.get("activityId");
        if (activityId == null) {
            return Result.error("缺少活动ID");
        }

        OfflineActivity activity = offlineActivityService.getById(activityId);
        if (activity == null || !"OPEN".equals(activity.getStatus())) {
            return Result.error("活动不存在或不可报名");
        }
        int bookedCount = activity.getBookedCount() == null ? 0 : activity.getBookedCount();
        int quota = activity.getQuota() == null ? 0 : activity.getQuota();
        if (bookedCount >= quota) {
            return Result.error("活动名额已满");
        }

        QueryWrapper<OfflineBooking> duplicateQuery = new QueryWrapper<>();
        duplicateQuery.eq("activity_id", activityId).eq("user_id", currentUser.getId());
        OfflineBooking existing = offlineBookingService.getOne(duplicateQuery);
        if (existing != null && !"CANCELED".equals(existing.getStatus())) {
            return Result.error("你已报名该活动");
        }

        if (existing == null) {
            OfflineBooking booking = new OfflineBooking();
            booking.setActivityId(activityId);
            booking.setUserId(currentUser.getId());
            booking.setStatus("BOOKED");
            booking.setPayAmount(activity.getPrice() == null ? BigDecimal.ZERO : activity.getPrice());
            booking.setCreateTime(LocalDateTime.now());
            booking.setUpdateTime(LocalDateTime.now());
            offlineBookingService.save(booking);
        } else {
            existing.setStatus("BOOKED");
            existing.setPayAmount(activity.getPrice() == null ? BigDecimal.ZERO : activity.getPrice());
            existing.setUpdateTime(LocalDateTime.now());
            offlineBookingService.updateById(existing);
        }

        activity.setBookedCount(bookedCount + 1);
        activity.setUpdateTime(LocalDateTime.now());
        offlineActivityService.updateById(activity);
        OperationLogUtils.record(operationLogService, currentUser, "OFFLINE", "BOOK_ACTIVITY", activityId, "报名线下活动《" + activity.getTitle() + "》");
        return Result.success("报名成功");
    }

    @GetMapping("/bookings/my")
    public Result<List<Map<String, Object>>> myBookings(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        QueryWrapper<OfflineBooking> query = new QueryWrapper<>();
        query.eq("user_id", currentUser.getId()).orderByDesc("create_time");
        return Result.success(toBookingPayload(offlineBookingService.list(query)));
    }

    @GetMapping("/bookings/all")
    public Result<List<Map<String, Object>>> allBookings(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return Result.error("仅管理员可查看全部报名");
        }
        QueryWrapper<OfflineBooking> query = new QueryWrapper<>();
        query.orderByDesc("create_time");
        return Result.success(toBookingPayload(offlineBookingService.list(query)));
    }

    @PutMapping("/bookings/{id}/status")
    public Result<String> updateBookingStatus(@PathVariable Long id,
                                              @RequestBody Map<String, String> body,
                                              @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return Result.error("仅管理员可更新报名状态");
        }

        OfflineBooking booking = offlineBookingService.getById(id);
        if (booking == null) {
            return Result.error("报名记录不存在");
        }
        String newStatus = String.valueOf(body.getOrDefault("status", "BOOKED")).toUpperCase();
        booking.setStatus(newStatus);
        booking.setUpdateTime(LocalDateTime.now());
        offlineBookingService.updateById(booking);
        OperationLogUtils.record(operationLogService, currentUser, "OFFLINE", "UPDATE_BOOKING_STATUS", id, "线下报名状态更新为 " + newStatus);
        return Result.success("状态更新成功");
    }

    private User currentUser(String authorization) {
        Long userId = AuthUtils.parseUserId(authorization);
        return userId == null ? null : userService.getById(userId);
    }

    private List<Map<String, Object>> toBookingPayload(List<OfflineBooking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> activityIds = bookings.stream().map(OfflineBooking::getActivityId).collect(Collectors.toSet());
        Set<Long> userIds = bookings.stream().map(OfflineBooking::getUserId).collect(Collectors.toSet());

        Map<Long, OfflineActivity> activityMap = offlineActivityService.listByIds(activityIds).stream()
                .collect(Collectors.toMap(OfflineActivity::getId, a -> a, (a, b) -> a));
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<Map<String, Object>> payload = new ArrayList<>();
        for (OfflineBooking booking : bookings) {
            OfflineActivity activity = activityMap.get(booking.getActivityId());
            User user = userMap.get(booking.getUserId());
            Map<String, Object> row = new HashMap<>();
            row.put("id", booking.getId());
            row.put("activityId", booking.getActivityId());
            row.put("activityTitle", activity == null ? "-" : activity.getTitle());
            row.put("activityTime", activity == null ? null : activity.getActivityTime());
            row.put("city", activity == null ? "-" : activity.getCity());
            row.put("location", activity == null ? "-" : activity.getLocation());
            row.put("userId", booking.getUserId());
            row.put("username", user == null ? "-" : user.getUsername());
            row.put("status", booking.getStatus());
            row.put("payAmount", booking.getPayAmount());
            row.put("createTime", booking.getCreateTime());
            payload.add(row);
        }
        return payload;
    }
}

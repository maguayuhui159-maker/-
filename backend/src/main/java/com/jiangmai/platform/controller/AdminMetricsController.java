package com.jiangmai.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiangmai.platform.entity.*;
import com.jiangmai.platform.service.*;
import com.jiangmai.platform.utils.AuthUtils;
import com.jiangmai.platform.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/metrics")
public class AdminMetricsController {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private WorkService workService;

    @Autowired
    private CourseEnrollmentService courseEnrollmentService;

    @Autowired
    private OrderRecordService orderRecordService;

    @Autowired
    private OfflineBookingService offlineBookingService;

    @Autowired
    private LearningHomeworkService learningHomeworkService;

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = AuthUtils.parseUserId(authorization);
        if (userId == null) {
            return Result.error("未登录或登录已失效");
        }
        User user = userService.getById(userId);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return Result.error("仅管理员可查看指标");
        }

        long totalUsers = userService.count();
        long totalCourses = courseService.count();
        long publishedCourses = courseService.count(new QueryWrapper<Course>().eq("status", "PUBLISHED"));
        long pendingCourses = courseService.count(new QueryWrapper<Course>().eq("status", "PENDING"));
        long totalWorks = workService.count();
        long approvedWorks = workService.count(new QueryWrapper<Work>().eq("status", "APPROVED"));
        long pendingWorks = workService.count(new QueryWrapper<Work>().eq("status", "PENDING"));
        long totalEnrollments = courseEnrollmentService.count();
        long totalOrders = 0;
        long offlineBookings = 0;
        long aiUsageCount = 0;
        long operationCount = 0;
        int totalStudyMinutes = 0;
        BigDecimal gmv = BigDecimal.ZERO;

        try {
            totalStudyMinutes = courseEnrollmentService.list().stream()
                    .map(CourseEnrollment::getStudyMinutes)
                    .filter(v -> v != null)
                    .reduce(0, Integer::sum);
        } catch (Exception ignored) {
        }
        try {
            totalOrders = orderRecordService.count();
            gmv = orderRecordService.list().stream()
                    .map(OrderRecord::getAmount)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception ignored) {
        }
        try {
            offlineBookings = offlineBookingService.count();
        } catch (Exception ignored) {
        }
        try {
            aiUsageCount = learningHomeworkService.count();
        } catch (Exception ignored) {
        }
        try {
            operationCount = operationLogService.count();
        } catch (Exception ignored) {
        }

        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", totalUsers);
        data.put("totalCourses", totalCourses);
        data.put("publishedCourses", publishedCourses);
        data.put("pendingCourses", pendingCourses);
        data.put("totalWorks", totalWorks);
        data.put("approvedWorks", approvedWorks);
        data.put("pendingWorks", pendingWorks);
        data.put("totalEnrollments", totalEnrollments);
        data.put("totalOrders", totalOrders);
        data.put("offlineBookings", offlineBookings);
        data.put("totalStudyHours", BigDecimal.valueOf(totalStudyMinutes).divide(BigDecimal.valueOf(60), 1, java.math.RoundingMode.HALF_UP));
        data.put("aiUsageCount", aiUsageCount);
        data.put("operationCount", operationCount);
        data.put("gmv", gmv);
        return Result.success(data);
    }
}

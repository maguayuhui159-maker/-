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
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profile")
public class ProfileMetricsController {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private WorkService workService;

    @Autowired
    private CourseEnrollmentService courseEnrollmentService;

    @Autowired
    private LearningHomeworkService learningHomeworkService;

    @Autowired
    private WorkFavoriteService workFavoriteService;

    @Autowired
    private WorkViewHistoryService workViewHistoryService;

    @Autowired
    private OrderRecordService orderRecordService;

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = AuthUtils.parseUserId(authorization);
        if (userId == null) {
            return Result.error("未登录或登录已失效");
        }
        User currentUser = userService.getById(userId);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("role", currentUser.getRole());
        data.put("username", currentUser.getUsername());
        data.put("nickname", currentUser.getNickname());

        if ("ADMIN".equals(currentUser.getRole())) {
            fillAdminSummary(data);
        } else if ("UPLOADER".equals(currentUser.getRole())) {
            fillUploaderSummary(currentUser, data);
        } else {
            fillStudentSummary(currentUser, data);
        }
        return Result.success(data);
    }

    private void fillStudentSummary(User currentUser, Map<String, Object> data) {
        QueryWrapper<CourseEnrollment> enrollmentQuery = new QueryWrapper<>();
        enrollmentQuery.eq("user_id", currentUser.getId()).eq("status", "PAID").orderByDesc("create_time");
        List<CourseEnrollment> enrollments = courseEnrollmentService.list(enrollmentQuery);
        Set<Long> courseIds = enrollments.stream().map(CourseEnrollment::getCourseId).collect(Collectors.toSet());
        Map<Long, Course> courseMap = courseIds.isEmpty()
                ? Collections.emptyMap()
                : courseService.listByIds(courseIds).stream().collect(Collectors.toMap(Course::getId, c -> c, (a, b) -> a));

        int totalStudyMinutes = enrollments.stream()
                .map(CourseEnrollment::getStudyMinutes)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);
        long activePurchasedCourses = enrollments.stream()
                .map(CourseEnrollment::getCourseId)
                .filter(courseMap::containsKey)
                .filter(courseId -> "PUBLISHED".equals(courseMap.get(courseId).getStatus()))
                .count();

        QueryWrapper<WorkFavorite> favoriteQuery = new QueryWrapper<>();
        favoriteQuery.eq("user_id", currentUser.getId()).orderByDesc("create_time");
        List<WorkFavorite> favorites = workFavoriteService.list(favoriteQuery);
        List<Map<String, Object>> favoriteWorks = toFavoriteWorks(favorites);

        QueryWrapper<WorkViewHistory> viewQuery = new QueryWrapper<>();
        viewQuery.eq("user_id", currentUser.getId()).orderByDesc("last_view_time").last("limit 6");
        List<WorkViewHistory> viewHistories = workViewHistoryService.list(viewQuery);

        QueryWrapper<LearningHomework> homeworkQuery = new QueryWrapper<>();
        homeworkQuery.eq("user_id", currentUser.getId());
        long homeworkCount = learningHomeworkService.count(homeworkQuery);

        data.put("purchasedCourseCount", activePurchasedCourses);
        data.put("studyHours", BigDecimal.valueOf(totalStudyMinutes).divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP));
        data.put("favoriteWorkCount", favoriteWorks.size());
        data.put("homeworkCount", homeworkCount);
        data.put("favoriteWorks", favoriteWorks);
        data.put("recentViewedWorks", toViewedWorks(viewHistories));
    }

    private void fillUploaderSummary(User currentUser, Map<String, Object> data) {
        QueryWrapper<Course> courseQuery = new QueryWrapper<>();
        courseQuery.eq("teacher_id", currentUser.getId()).orderByDesc("create_time");
        List<Course> courses = courseService.list(courseQuery);
        Set<Long> teacherCourseIds = courses.stream().map(Course::getId).collect(Collectors.toSet());

        QueryWrapper<Work> workQuery = new QueryWrapper<>();
        workQuery.eq("author_id", currentUser.getId()).orderByDesc("create_time");
        List<Work> works = workService.list(workQuery);

        BigDecimal averageScore = BigDecimal.ZERO;
        if (!teacherCourseIds.isEmpty()) {
            QueryWrapper<LearningHomework> homeworkQuery = new QueryWrapper<>();
            homeworkQuery.in("course_id", teacherCourseIds).eq("status", "REVIEWED");
            List<LearningHomework> homeworkList = learningHomeworkService.list(homeworkQuery);
            List<Integer> scores = homeworkList.stream().map(LearningHomework::getScore).filter(Objects::nonNull).collect(Collectors.toList());
            if (!scores.isEmpty()) {
                averageScore = BigDecimal.valueOf(scores.stream().reduce(0, Integer::sum))
                        .divide(BigDecimal.valueOf(scores.size()), 1, RoundingMode.HALF_UP);
            }
        }

        QueryWrapper<OperationLog> logQuery = new QueryWrapper<>();
        logQuery.eq("actor_id", currentUser.getId()).orderByDesc("create_time").last("limit 6");
        List<OperationLog> recentLogs = operationLogService.list(logQuery);

        data.put("uploadedCourseCount", courses.size());
        data.put("publishedCourseCount", courses.stream().filter(c -> "PUBLISHED".equals(c.getStatus())).count());
        data.put("pendingCourseCount", courses.stream().filter(c -> "PENDING".equals(c.getStatus())).count());
        data.put("uploadedWorkCount", works.size());
        data.put("approvedWorkCount", works.stream().filter(w -> "APPROVED".equals(w.getStatus())).count());
        data.put("pendingWorkCount", works.stream().filter(w -> "PENDING".equals(w.getStatus())).count());
        data.put("averageScore", averageScore);
        data.put("recentOperations", toOperationLogs(recentLogs));
    }

    private void fillAdminSummary(Map<String, Object> data) {
        long totalUsers = userService.count();
        long totalCourses = courseService.count();
        long pendingCourses = courseService.count(new QueryWrapper<Course>().eq("status", "PENDING"));
        long totalWorks = workService.count();
        long pendingWorks = workService.count(new QueryWrapper<Work>().eq("status", "PENDING"));
        long totalOrders = orderRecordService.count();

        QueryWrapper<OperationLog> logQuery = new QueryWrapper<>();
        logQuery.orderByDesc("create_time").last("limit 8");
        List<OperationLog> recentLogs = operationLogService.list(logQuery);

        data.put("totalUsers", totalUsers);
        data.put("totalCourses", totalCourses);
        data.put("pendingCourses", pendingCourses);
        data.put("totalWorks", totalWorks);
        data.put("pendingWorks", pendingWorks);
        data.put("totalOrders", totalOrders);
        data.put("recentOperations", toOperationLogs(recentLogs));
    }

    private List<Map<String, Object>> toFavoriteWorks(List<WorkFavorite> favorites) {
        if (favorites == null || favorites.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> workIds = favorites.stream().map(WorkFavorite::getWorkId).collect(Collectors.toSet());
        Map<Long, Work> workMap = workService.listByIds(workIds).stream()
                .filter(work -> "APPROVED".equals(work.getStatus()))
                .collect(Collectors.toMap(Work::getId, work -> work, (a, b) -> a));

        List<Map<String, Object>> payload = new ArrayList<>();
        for (WorkFavorite favorite : favorites) {
            Work work = workMap.get(favorite.getWorkId());
            if (work == null) {
                continue;
            }
            payload.add(toWorkPayload(work, favorite.getCreateTime(), null, 0));
        }
        return payload;
    }

    private List<Map<String, Object>> toViewedWorks(List<WorkViewHistory> histories) {
        if (histories == null || histories.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> workIds = histories.stream().map(WorkViewHistory::getWorkId).collect(Collectors.toSet());
        Map<Long, Work> workMap = workService.listByIds(workIds).stream()
                .collect(Collectors.toMap(Work::getId, work -> work, (a, b) -> a));

        List<Map<String, Object>> payload = new ArrayList<>();
        for (WorkViewHistory history : histories) {
            Work work = workMap.get(history.getWorkId());
            if (work == null) {
                continue;
            }
            payload.add(toWorkPayload(work, null, history.getLastViewTime(), history.getViewCount() == null ? 0 : history.getViewCount()));
        }
        return payload;
    }

    private Map<String, Object> toWorkPayload(Work work, Object favoriteTime, Object lastViewTime, int viewCount) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", work.getId());
        row.put("title", work.getTitle());
        row.put("description", work.getDescription());
        row.put("imageUrl", work.getImageUrl());
        row.put("status", work.getStatus());
        row.put("isForSale", work.getIsForSale());
        row.put("price", work.getPrice());
        row.put("createTime", work.getCreateTime());
        row.put("favoriteTime", favoriteTime);
        row.put("lastViewTime", lastViewTime);
        row.put("viewCount", viewCount);
        return row;
    }

    private List<Map<String, Object>> toOperationLogs(List<OperationLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> payload = new ArrayList<>();
        for (OperationLog log : logs) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", log.getId());
            row.put("actorName", log.getActorName());
            row.put("actorRole", log.getActorRole());
            row.put("module", log.getModule());
            row.put("action", log.getAction());
            row.put("targetId", log.getTargetId());
            row.put("detail", log.getDetail());
            row.put("createTime", log.getCreateTime());
            payload.add(row);
        }
        return payload;
    }
}

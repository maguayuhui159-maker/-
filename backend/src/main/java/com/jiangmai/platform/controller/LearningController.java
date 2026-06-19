package com.jiangmai.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiangmai.platform.entity.Course;
import com.jiangmai.platform.entity.CourseEnrollment;
import com.jiangmai.platform.entity.LearningHomework;
import com.jiangmai.platform.entity.User;
import com.jiangmai.platform.service.CourseEnrollmentService;
import com.jiangmai.platform.service.CourseService;
import com.jiangmai.platform.service.LearningHomeworkService;
import com.jiangmai.platform.service.OperationLogService;
import com.jiangmai.platform.service.UserService;
import com.jiangmai.platform.utils.AuthUtils;
import com.jiangmai.platform.utils.OperationLogUtils;
import com.jiangmai.platform.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/learning")
public class LearningController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseEnrollmentService enrollmentService;

    @Autowired
    private LearningHomeworkService learningHomeworkService;

    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogService operationLogService;

    @PostMapping("/purchase")
    public Result<String> purchaseCourse(@RequestBody Map<String, Long> body,
                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        Long courseId = body.get("courseId");
        if (courseId == null) {
            return Result.error("缺少课程ID");
        }

        Course course = courseService.getById(courseId);
        if (course == null || !"PUBLISHED".equals(course.getStatus())) {
            return Result.error("课程不存在或未发布");
        }

        QueryWrapper<CourseEnrollment> query = new QueryWrapper<>();
        query.eq("user_id", currentUser.getId()).eq("course_id", courseId);
        CourseEnrollment existing = enrollmentService.getOne(query);
        if (existing != null) {
            if (!"PAID".equals(existing.getStatus())) {
                existing.setStatus("PAID");
                existing.setUpdateTime(LocalDateTime.now());
                enrollmentService.updateById(existing);
            }
            return Result.success("已购买该课程");
        }

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setUserId(currentUser.getId());
        enrollment.setCourseId(courseId);
        enrollment.setStatus("PAID");
        enrollment.setPayAmount(course.getPrice());
        enrollment.setProgress(0);
        enrollment.setStudyMinutes(0);
        enrollment.setLastStudyTime(LocalDateTime.now());
        enrollment.setCreateTime(LocalDateTime.now());
        enrollment.setUpdateTime(LocalDateTime.now());
        enrollmentService.save(enrollment);
        OperationLogUtils.record(operationLogService, currentUser, "COURSE", "PURCHASE", courseId, "购买课程《" + course.getTitle() + "》");
        return Result.success("购买成功");
    }

    @GetMapping("/my-courses")
    public Result<List<Map<String, Object>>> myCourses(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }

        QueryWrapper<CourseEnrollment> query = new QueryWrapper<>();
        query.eq("user_id", currentUser.getId()).eq("status", "PAID").orderByDesc("create_time");
        List<CourseEnrollment> enrollments = enrollmentService.list(query);
        if (enrollments.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        List<Long> courseIds = enrollments.stream().map(CourseEnrollment::getCourseId).collect(Collectors.toList());
        List<Course> courses = courseService.listByIds(courseIds);
        Map<Long, Course> courseMap = courses.stream().collect(Collectors.toMap(Course::getId, c -> c, (a, b) -> a));

        List<Map<String, Object>> payload = new ArrayList<>();
        for (CourseEnrollment enrollment : enrollments) {
            Course course = courseMap.get(enrollment.getCourseId());
            if (course == null) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("enrollmentId", enrollment.getId());
            row.put("courseId", course.getId());
            row.put("title", course.getTitle());
            row.put("description", course.getDescription());
            row.put("coverUrl", course.getCoverUrl());
            row.put("videoUrl", course.getVideoUrl());
            row.put("price", course.getPrice());
            row.put("teacherId", course.getTeacherId());
            row.put("status", course.getStatus());
            row.put("progress", enrollment.getProgress());
            row.put("studyMinutes", enrollment.getStudyMinutes());
            row.put("lastStudyTime", enrollment.getLastStudyTime());
            row.put("purchaseTime", enrollment.getCreateTime());
            payload.add(row);
        }
        return Result.success(payload);
    }

    @PutMapping("/progress")
    public Result<String> updateProgress(@RequestBody Map<String, Integer> body,
                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        Integer courseIdInt = body.get("courseId");
        if (courseIdInt == null) {
            return Result.error("缺少课程ID");
        }

        int newProgress = Math.max(0, Math.min(100, body.getOrDefault("progress", 0)));
        int addMinutes = Math.max(0, body.getOrDefault("studyMinutes", 0));

        QueryWrapper<CourseEnrollment> query = new QueryWrapper<>();
        query.eq("user_id", currentUser.getId()).eq("course_id", courseIdInt.longValue()).eq("status", "PAID");
        CourseEnrollment enrollment = enrollmentService.getOne(query);
        if (enrollment == null) {
            return Result.error("请先购买课程");
        }

        int currentProgress = enrollment.getProgress() == null ? 0 : enrollment.getProgress();
        int currentMinutes = enrollment.getStudyMinutes() == null ? 0 : enrollment.getStudyMinutes();
        enrollment.setProgress(Math.max(currentProgress, newProgress));
        enrollment.setStudyMinutes(currentMinutes + addMinutes);
        enrollment.setLastStudyTime(LocalDateTime.now());
        enrollment.setUpdateTime(LocalDateTime.now());
        enrollmentService.updateById(enrollment);
        return Result.success("学习进度已更新");
    }

    @PostMapping("/homework")
    public Result<String> submitHomework(@RequestBody Map<String, Object> body,
                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        Object courseObj = body.get("courseId");
        Long courseId = courseObj == null ? null : Long.valueOf(String.valueOf(courseObj));
        if (courseId == null) {
            return Result.error("缺少课程ID");
        }

        QueryWrapper<CourseEnrollment> enrollmentQuery = new QueryWrapper<>();
        enrollmentQuery.eq("user_id", currentUser.getId()).eq("course_id", courseId).eq("status", "PAID");
        if (enrollmentService.getOne(enrollmentQuery) == null) {
            return Result.error("请先购买课程再提交作业");
        }

        try {
            LearningHomework homework = new LearningHomework();
            homework.setUserId(currentUser.getId());
            homework.setCourseId(courseId);
            homework.setContent(String.valueOf(body.getOrDefault("content", "")));
            homework.setWorkUrl(String.valueOf(body.getOrDefault("workUrl", "")));
            homework.setStatus("PENDING");
            homework.setAiComment(simulateAiComment(homework.getContent()));
            homework.setCreateTime(LocalDateTime.now());
            homework.setUpdateTime(LocalDateTime.now());
            learningHomeworkService.save(homework);
            OperationLogUtils.record(operationLogService, currentUser, "HOMEWORK", "SUBMIT", homework.getId(), "提交课程作业");
            return Result.success("作业提交成功");
        } catch (Exception e) {
            return Result.error("作业提交失败，请稍后重试");
        }
    }

    @GetMapping("/homework/my")
    public Result<List<Map<String, Object>>> myHomework(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        QueryWrapper<LearningHomework> query = new QueryWrapper<>();
        query.eq("user_id", currentUser.getId()).orderByDesc("create_time");
        return Result.success(enrichHomeworkList(learningHomeworkService.list(query)));
    }

    @GetMapping("/homework/all")
    public Result<List<Map<String, Object>>> allHomework(@RequestParam(required = false) String status,
                                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        if (!"ADMIN".equals(currentUser.getRole()) && !"UPLOADER".equals(currentUser.getRole())) {
            return Result.error("无权限查看全部作业");
        }

        QueryWrapper<LearningHomework> query = new QueryWrapper<>();
        if (status != null && !status.isBlank()) {
            query.eq("status", status.trim().toUpperCase());
        }
        query.orderByDesc("create_time");
        List<LearningHomework> list = learningHomeworkService.list(query);

        if ("UPLOADER".equals(currentUser.getRole())) {
            Set<Long> teacherCourseIds = courseService.list(new QueryWrapper<Course>().eq("teacher_id", currentUser.getId())).stream()
                    .map(Course::getId)
                    .collect(Collectors.toSet());
            list = list.stream().filter(item -> teacherCourseIds.contains(item.getCourseId())).collect(Collectors.toList());
        }
        return Result.success(enrichHomeworkList(list));
    }

    @PutMapping("/homework/{id}/review")
    public Result<String> reviewHomework(@PathVariable Long id,
                                         @RequestBody Map<String, Object> body,
                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        if (!"ADMIN".equals(currentUser.getRole()) && !"UPLOADER".equals(currentUser.getRole())) {
            return Result.error("无权限审核作业");
        }

        LearningHomework homework = learningHomeworkService.getById(id);
        if (homework == null) {
            return Result.error("作业不存在");
        }
        Course course = courseService.getById(homework.getCourseId());
        if (course == null) {
            return Result.error("课程不存在");
        }
        if ("UPLOADER".equals(currentUser.getRole()) && !Objects.equals(course.getTeacherId(), currentUser.getId())) {
            return Result.error("仅课程讲师可点评该作业");
        }

        homework.setTeacherComment(String.valueOf(body.getOrDefault("teacherComment", "")));
        Object scoreObj = body.get("score");
        if (scoreObj != null) {
            try {
                int score = Integer.parseInt(String.valueOf(scoreObj));
                homework.setScore(Math.max(0, Math.min(100, score)));
            } catch (Exception ignored) {
            }
        }
        homework.setStatus("REVIEWED");
        homework.setUpdateTime(LocalDateTime.now());
        learningHomeworkService.updateById(homework);
        OperationLogUtils.record(operationLogService, currentUser, "HOMEWORK", "REVIEW", id, "点评课程作业");
        return Result.success("点评成功");
    }

    private User currentUser(String authorization) {
        Long userId = AuthUtils.parseUserId(authorization);
        return userId == null ? null : userService.getById(userId);
    }

    private List<Map<String, Object>> enrichHomeworkList(List<LearningHomework> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> userIds = list.stream().map(LearningHomework::getUserId).collect(Collectors.toSet());
        Set<Long> courseIds = list.stream().map(LearningHomework::getCourseId).collect(Collectors.toSet());
        Map<Long, String> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));
        Map<Long, String> courseMap = courseService.listByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::getTitle, (a, b) -> a));

        List<Map<String, Object>> payload = new ArrayList<>();
        for (LearningHomework item : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", item.getId());
            row.put("userId", item.getUserId());
            row.put("username", userMap.getOrDefault(item.getUserId(), "-"));
            row.put("courseId", item.getCourseId());
            row.put("courseTitle", courseMap.getOrDefault(item.getCourseId(), "-"));
            row.put("content", item.getContent());
            row.put("workUrl", item.getWorkUrl());
            row.put("status", item.getStatus());
            row.put("aiComment", item.getAiComment());
            row.put("teacherComment", item.getTeacherComment());
            row.put("score", item.getScore());
            row.put("createTime", item.getCreateTime());
            payload.add(row);
        }
        return payload;
    }

    private String simulateAiComment(String content) {
        String text = content == null ? "" : content;
        if (text.length() < 10) {
            return "作业说明较短，建议补充工艺步骤、工具参数和遇到的问题，便于精准点评。";
        }
        return "整体完成度良好，建议加强纹样转角过渡和抛光一致性，下一次可增加工序对比说明。";
    }
}

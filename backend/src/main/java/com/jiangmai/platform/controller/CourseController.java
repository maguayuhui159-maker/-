package com.jiangmai.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiangmai.platform.dto.CourseDTO;
import com.jiangmai.platform.entity.Course;
import com.jiangmai.platform.entity.User;
import com.jiangmai.platform.service.CourseService;
import com.jiangmai.platform.service.OperationLogService;
import com.jiangmai.platform.service.UserService;
import com.jiangmai.platform.utils.AuthUtils;
import com.jiangmai.platform.utils.OperationLogUtils;
import com.jiangmai.platform.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping
    public Result<List<Course>> listCourses() {
        QueryWrapper<Course> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED").orderByDesc("create_time");
        return Result.success(courseService.list(wrapper));
    }

    @GetMapping("/all")
    public Result<List<Course>> listAllCourses(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        QueryWrapper<Course> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (currentUser == null || "STUDENT".equals(currentUser.getRole())) {
            wrapper.eq("status", "PUBLISHED");
        } else if ("UPLOADER".equals(currentUser.getRole())) {
            wrapper.eq("teacher_id", currentUser.getId());
        }
        return Result.success(courseService.list(wrapper));
    }

    @GetMapping("/my-uploaded")
    public Result<List<Course>> myUploadedCourses(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        QueryWrapper<Course> wrapper = new QueryWrapper<>();
        wrapper.eq("teacher_id", currentUser.getId()).orderByDesc("create_time");
        return Result.success(courseService.list(wrapper));
    }

    @PostMapping("/upload")
    public Result<String> uploadCourse(@RequestBody CourseDTO courseDTO,
                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        if (!"UPLOADER".equals(currentUser.getRole())) {
            return Result.error("仅讲师可发布课程");
        }
        try {
            courseService.createCourse(courseDTO, currentUser.getId());
            OperationLogUtils.record(operationLogService, currentUser, "COURSE", "UPLOAD", null, "提交课程《" + courseDTO.getTitle() + "》");
            return Result.success("上传课程成功等待审核");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/review")
    public Result<String> reviewCourse(@PathVariable Long id,
                                       @RequestBody java.util.Map<String, String> body,
                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        if (!"ADMIN".equals(currentUser.getRole())) {
            return Result.error("仅管理员可审核课程");
        }

        String newStatus = String.valueOf(body.getOrDefault("status", "")).trim().toUpperCase();
        if (!"PUBLISHED".equals(newStatus) && !"REJECTED".equals(newStatus) && !"OFFLINE".equals(newStatus)) {
            return Result.error("审核状态非法");
        }
        Course course = courseService.getById(id);
        if (course == null) {
            return Result.error("课程不存在");
        }
        course.setStatus(newStatus);
        courseService.updateById(course);
        OperationLogUtils.record(operationLogService, currentUser, "COURSE", "REVIEW", id, "课程《" + course.getTitle() + "》审核为 " + newStatus);
        return Result.success("审核操作成功");
    }

    private User currentUser(String authorization) {
        Long userId = AuthUtils.parseUserId(authorization);
        return userId == null ? null : userService.getById(userId);
    }
}

package com.jiangmai.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiangmai.platform.dto.CourseDTO;
import com.jiangmai.platform.entity.Course;

public interface CourseService extends IService<Course> {
    void createCourse(CourseDTO courseDTO, Long teacherId);
}

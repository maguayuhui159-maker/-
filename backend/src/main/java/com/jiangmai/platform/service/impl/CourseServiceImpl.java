package com.jiangmai.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiangmai.platform.dto.CourseDTO;
import com.jiangmai.platform.entity.Course;
import com.jiangmai.platform.mapper.CourseMapper;
import com.jiangmai.platform.service.CourseService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Override
    public void createCourse(CourseDTO courseDTO, Long teacherId) {
        Course course = new Course();
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setCoverUrl(courseDTO.getCoverUrl());
        course.setVideoUrl(courseDTO.getVideoUrl());
        course.setPrice(courseDTO.getPrice());
        course.setStatus(courseDTO.getStatus() != null ? courseDTO.getStatus() : "DRAFT");
        course.setTeacherId(teacherId);
        course.setCreateTime(LocalDateTime.now());
        
        this.save(course);
    }
}

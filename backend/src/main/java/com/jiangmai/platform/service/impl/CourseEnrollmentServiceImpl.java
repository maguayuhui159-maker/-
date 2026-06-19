package com.jiangmai.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiangmai.platform.entity.CourseEnrollment;
import com.jiangmai.platform.mapper.CourseEnrollmentMapper;
import com.jiangmai.platform.service.CourseEnrollmentService;
import org.springframework.stereotype.Service;

@Service
public class CourseEnrollmentServiceImpl extends ServiceImpl<CourseEnrollmentMapper, CourseEnrollment> implements CourseEnrollmentService {
}

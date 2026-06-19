package com.jiangmai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course_enrollment")
public class CourseEnrollment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long courseId;

    private String status;

    private BigDecimal payAmount;

    private Integer progress;

    private Integer studyMinutes;

    private LocalDateTime lastStudyTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

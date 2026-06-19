package com.jiangmai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("learning_homework")
public class LearningHomework implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long courseId;

    private String content;

    private String workUrl;

    private String status;

    private String aiComment;

    private String teacherComment;

    private Integer score;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

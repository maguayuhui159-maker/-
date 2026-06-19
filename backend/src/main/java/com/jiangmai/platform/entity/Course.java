package com.jiangmai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String coverUrl;

    private String videoUrl;

    private Long teacherId;

    private BigDecimal price;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

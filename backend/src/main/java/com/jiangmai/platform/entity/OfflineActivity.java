package com.jiangmai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("offline_activity")
public class OfflineActivity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String city;

    private String location;

    private LocalDateTime activityTime;

    private Integer quota;

    private Integer bookedCount;

    private BigDecimal price;

    private String status;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

package com.jiangmai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("profile_change_request")
public class ProfileChangeRequest implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String currentUsername;

    private String requestedUsername;

    private String requestedAvatar;

    private String status;

    private String reviewComment;

    private String reviewedBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}


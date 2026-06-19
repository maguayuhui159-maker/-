package com.jiangmai.platform.dto;

import lombok.Data;

@Data
public class ProfileChangeSubmitDTO {
    private String currentUsername;
    private String requestedUsername;
    private String requestedNickname;
    private String requestedAvatar;
}

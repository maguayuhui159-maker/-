package com.jiangmai.platform.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String nickname;
    private String role; // ADMIN, STUDENT, UPLOADER
}

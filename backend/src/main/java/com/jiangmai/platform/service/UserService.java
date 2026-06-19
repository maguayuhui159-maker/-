package com.jiangmai.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiangmai.platform.dto.LoginDTO;
import com.jiangmai.platform.dto.RegisterDTO;
import com.jiangmai.platform.entity.User;

public interface UserService extends IService<User> {
    String login(LoginDTO loginDTO);
    void register(RegisterDTO registerDTO);
}

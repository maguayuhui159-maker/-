package com.jiangmai.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiangmai.platform.dto.LoginDTO;
import com.jiangmai.platform.dto.RegisterDTO;
import com.jiangmai.platform.entity.User;
import com.jiangmai.platform.mapper.UserMapper;
import com.jiangmai.platform.service.UserService;
import com.jiangmai.platform.utils.JwtUtils;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private boolean isBcryptHash(String value) {
        if (value == null) return false;
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    @Override
    public String login(LoginDTO loginDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginDTO.getUsername());
        User user = this.getOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        String dbPassword = user.getPassword();
        String inputPassword = loginDTO.getPassword();
        if (inputPassword == null || inputPassword.isBlank()) {
            throw new RuntimeException("密码不能为空");
        }

        boolean valid;
        if (isBcryptHash(dbPassword)) {
            valid = PASSWORD_ENCODER.matches(inputPassword, dbPassword);
        } else {
            valid = dbPassword != null && dbPassword.equals(inputPassword);
            if (valid) {
                user.setPassword(PASSWORD_ENCODER.encode(inputPassword));
                this.updateById(user);
            }
        }

        if (!valid) {
            throw new RuntimeException("用户名或密码错误");
        }

        return JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        String username = registerDTO.getUsername() == null ? "" : registerDTO.getUsername().trim();
        if (username.isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        if (this.count(queryWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        if (registerDTO.getPassword() == null || registerDTO.getPassword().isBlank()) {
            throw new RuntimeException("密码不能为空");
        }
        user.setPassword(PASSWORD_ENCODER.encode(registerDTO.getPassword()));
        String nickname = registerDTO.getNickname() == null ? "" : registerDTO.getNickname().trim();
        user.setNickname(nickname.isEmpty() ? username : nickname);
        
        // Public registration cannot directly create an admin account.
        String role = registerDTO.getRole() != null ? registerDTO.getRole().toUpperCase() : "STUDENT";
        if (!role.equals("STUDENT") && !role.equals("UPLOADER")) {
            role = "STUDENT";
        }
        user.setRole(role);
        
        user.setCreateTime(LocalDateTime.now());
        
        this.save(user);
    }
}

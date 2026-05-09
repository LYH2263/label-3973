package com.tcm.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.health.dto.LoginDTO;
import com.tcm.health.dto.RegisterDTO;
import com.tcm.health.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> login(LoginDTO dto);
    User register(RegisterDTO dto);
}

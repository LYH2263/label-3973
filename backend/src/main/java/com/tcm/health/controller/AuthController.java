package com.tcm.health.controller;

import com.tcm.health.common.R;
import com.tcm.health.dto.LoginDTO;
import com.tcm.health.dto.RegisterDTO;
import com.tcm.health.entity.User;
import com.tcm.health.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证接口", description = "登录与注册")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        try {
            return R.ok(userService.login(dto));
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public R<User> register(@Valid @RequestBody RegisterDTO dto) {
        try {
            return R.ok(userService.register(dto));
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }
}

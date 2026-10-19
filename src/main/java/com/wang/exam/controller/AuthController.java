package com.wang.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.exam.common.JwtUtil;
import com.wang.exam.common.Result;
import com.wang.exam.entity.User;
import com.wang.exam.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
//处理登陆注册有关的的请求
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    //创建密码加密器
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //注册
    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.fail("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return Result.fail("密码至少 6 位");
        }

        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())
        );
        if (count > 0) {
            return Result.fail("用户名已存在");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return Result.success(null);
    }

    //登录
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        User dbUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())
        );
        if (dbUser == null) {
            return Result.fail("用户名或密码错误");
        }

        if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            return Result.fail("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(dbUser.getId(), dbUser.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", Map.of(
                "id", dbUser.getId(),
                "username", dbUser.getUsername()
        ));
        return Result.success(data);
    }
}
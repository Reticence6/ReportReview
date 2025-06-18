package com.wjk.reportsreview.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wjk.reportsreview.common.Result;
import com.wjk.reportsreview.entity.User;
import com.wjk.reportsreview.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     * @param loginMap 包含用户名和密码的Map
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginMap) {
        String username = loginMap.get("username");
        String password = loginMap.get("password");
        
        // 参数校验
        if (username == null || password == null) {
            return Result.error(1001, "用户名或密码不能为空");
        }
        
        // 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        
        // 验证用户存在且密码正确
        if (user == null || !password.equals(user.getPassword())) {
            return Result.error(1002, "用户名或密码错误");
        }
        
        // 生成token（实际项目中应使用JWT等方式）
        String token = "mock_token_" + System.currentTimeMillis();
        
        // 返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("token", token);
        
        // 用户信息（不返回密码）
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("name", user.getName());
        userMap.put("role", user.getRole());
        resultMap.put("user", userMap);
        
        return Result.success("登录成功", resultMap);
    }
    
    /**
     * 用户登出
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<?> logout() {
        // 实际项目中应该处理token失效等逻辑
        return Result.success("登出成功");
    }
} 
package com.example.booksystem.config;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.booksystem.entity.User;
import com.example.booksystem.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, "admin")
        );

        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(1);
            admin.setStatus(1);
            admin.setEmail("admin@qq.com");
            admin.setRealName("管理员");
            admin.setCreateTime(new Date());
            userMapper.insert(admin);
            log.info("管理员账号初始化成功");
        } else {
            // 更新密码为123456
            admin.setPassword(passwordEncoder.encode("123456"));
            userMapper.updateById(admin);
            log.info("管理员密码更新成功");
        }
    }
} 
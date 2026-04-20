package com.example.booksystem.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.booksystem.entity.User;
import com.example.booksystem.exception.BusinessException;
import com.example.booksystem.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * UserDetailsService实现类
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户信息
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        // 用户被禁用
        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("账号已被禁用");
        }

        // 创建权限列表
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        // 根据用户角色添加权限
        if (user.getRole() == 1) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities);
    }
}
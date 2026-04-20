package com.example.booksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.booksystem.common.ResultCode;
import com.example.booksystem.config.JwtConfig;
import com.example.booksystem.dto.UserLoginDTO;
import com.example.booksystem.dto.UserRegisterDTO;
import com.example.booksystem.entity.User;
import com.example.booksystem.exception.BusinessException;
import com.example.booksystem.mapper.UserMapper;
import com.example.booksystem.service.UserService;
import com.example.booksystem.utils.JwtTokenUtil;
import com.example.booksystem.vo.LoginVO;
import com.example.booksystem.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(UserRegisterDTO userRegisterDTO) {
        // 校验用户名是否已存在
        User existUser = getUserByUsername(userRegisterDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(userRegisterDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        user.setPhone(userRegisterDTO.getPhone());
        user.setEmail(userRegisterDTO.getEmail());
        user.setRole(0); // 默认为普通用户
        user.setStatus(1); // 默认启用
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        return save(user);
    }

    @Override
    public LoginVO login(UserLoginDTO userLoginDTO) {
        // 认证
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userLoginDTO.getUsername(), userLoginDTO.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenUtil.generateToken(userDetails);

        // 获取用户信息
        User user = getUserByUsername(userDetails.getUsername());
        UserVO userVO = convertToUserVO(user);

        return new LoginVO(token, jwtConfig.getTokenHead(), userVO);
    }

    @Override
    public UserVO getCurrentUser() {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        User user = getUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return convertToUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserInfo(User user) {
        // 获取当前登录用户
        UserVO currentUser = getCurrentUser();
        User updateUser = getById(currentUser.getId());
        if (updateUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户信息
        updateUser.setRealName(user.getRealName());
        updateUser.setPhone(user.getPhone());
        updateUser.setEmail(user.getEmail());
        updateUser.setUpdateTime(new Date());

        return updateById(updateUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(String oldPassword, String newPassword) {
        // 获取当前登录用户
        UserVO currentUser = getCurrentUser();
        User user = getById(currentUser.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(new Date());

        return updateById(user);
    }

    @Override
    public User getUserByUsername(String username) {
        return getOne(new QueryWrapper<User>().eq("username", username));
    }

    @Override
    public UserVO convertToUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
}
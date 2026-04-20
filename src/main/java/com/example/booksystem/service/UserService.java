package com.example.booksystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.booksystem.dto.UserLoginDTO;
import com.example.booksystem.dto.UserRegisterDTO;
import com.example.booksystem.entity.User;
import com.example.booksystem.vo.LoginVO;
import com.example.booksystem.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterDTO 用户注册信息
     * @return 注册结果
     */
    boolean register(UserRegisterDTO userRegisterDTO);

    /**
     * 用户登录
     *
     * @param userLoginDTO 用户登录信息
     * @return 登录结果
     */
    LoginVO login(UserLoginDTO userLoginDTO);

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    UserVO getCurrentUser();

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 更新结果
     */
    boolean updateUserInfo(User user);

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    boolean updatePassword(String oldPassword, String newPassword);

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 转换为UserVO
     *
     * @param user 用户实体
     * @return UserVO
     */
    UserVO convertToUserVO(User user);
}
package com.example.booksystem.controller;

import com.example.booksystem.common.Result;
import com.example.booksystem.dto.UserLoginDTO;
import com.example.booksystem.dto.UserRegisterDTO;
import com.example.booksystem.entity.User;
import com.example.booksystem.service.UserService;
import com.example.booksystem.vo.LoginVO;
import com.example.booksystem.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 用户控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<Boolean> register(@Validated @RequestBody UserRegisterDTO userRegisterDTO) {
        boolean result = userService.register(userRegisterDTO);
        return Result.success(result, "注册成功");
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody UserLoginDTO userLoginDTO) {
        System.out.println("请求登录");
        LoginVO loginVO = userService.login(userLoginDTO);
        return Result.success(loginVO, "登录成功");
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> getCurrentUser() {
        UserVO userVO = userService.getCurrentUser();
        return Result.success(userVO);
    }

    @ApiOperation("更新用户信息")
    @PutMapping("/info")
    public Result<Boolean> updateUserInfo(@RequestBody User user) {
        boolean result = userService.updateUserInfo(user);
        return Result.success(result, "更新成功");
    }

    @ApiOperation("修改密码")
    @PutMapping("/password")
    public Result<Boolean> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        boolean result = userService.updatePassword(oldPassword, newPassword);
        return Result.success(result, "修改成功");
    }

    @ApiOperation("获取用户列表（管理员）")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Object> listUsers() {
        return Result.success(userService.list());
    }

    @ApiOperation("更新用户状态（管理员）")
    @PutMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> updateUserStatus(@RequestParam Long userId, @RequestParam Integer status) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        boolean result = userService.updateById(user);
        return Result.success(result, "更新成功");
    }

    @ApiOperation("创建用户（管理员）")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> createUser(@Validated @RequestBody User user) {
        // 检查用户名是否已存在
        if (userService.getUserByUsername(user.getUsername()) != null) {
            return Result.error("用户名已存在");
        }

        // 设置创建时间
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        // 对密码进行加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        boolean result = userService.save(user);
        return Result.success(result, "创建成功");
    }

    @ApiOperation("更新用户（管理员）")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> updateUser(@Validated @RequestBody User user) {
        User existUser = userService.getById(user.getId());
        if (existUser == null) {
            return Result.error("用户不存在");
        }

        // 不允许修改用户名
        user.setUsername(existUser.getUsername());
        // 不更新密码
        user.setPassword(existUser.getPassword());
        // 设置更新时间
        user.setUpdateTime(new Date());

        boolean result = userService.updateById(user);
        return Result.success(result, "更新成功");
    }

    @ApiOperation("获取用户详情（管理员）")
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> getUserById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @ApiOperation("删除用户（管理员）")
    @DeleteMapping("/detail/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 不允许删除管理员
        if (user.getRole() == 1) {
            return Result.error("不能删除管理员用户");
        }

        boolean result = userService.removeById(id);
        return Result.success(result, "删除成功");
    }
}
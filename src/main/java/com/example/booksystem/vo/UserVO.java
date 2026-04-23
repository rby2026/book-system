package com.example.booksystem.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息VO
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户角色：0-普通用户，1-管理员
     */
    private Integer role;

    /**
     * 用户类型：0-学生，1-教师
     */
    private Integer userType;

    /**
     * 账号状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;
}
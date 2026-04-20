package com.example.booksystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 违章信息实体类
 */
@Data
@TableName("t_violation")
public class Violation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 违章ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 违章类型：1-未按时归还，2-损坏图书，3-丢失图书
     */
    @TableField("violation_type")
    private Integer violationType;

    /**
     * 违章描述
     */
    private String description;

    /**
     * 相关图书ID
     */
    @TableField("book_id")
    private Long bookId;

    /**
     * 相关图书名称
     */
    @TableField("book_title")
    private String bookTitle;

    /**
     * 罚款金额
     */
    private Double fine;

    /**
     * 处理状态：0-未处理，1-已处理
     */
    @TableField("process_status")
    private Integer processStatus;

    /**
     * 违章时间
     */
    @TableField("violation_time")
    private Date violationTime;

    /**
     * 处理时间
     */
    @TableField("process_time")
    private Date processTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;
}
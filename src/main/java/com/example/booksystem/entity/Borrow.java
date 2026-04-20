package com.example.booksystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 借阅记录实体类
 */
@Data
@TableName("t_borrow")
public class Borrow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 借阅ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 图书ID
     */
    @TableField("book_id")
    private Long bookId;

    /**
     * 借阅时间
     */
    @TableField("borrow_time")
    private Date borrowTime;

    /**
     * 应还时间
     */
    @TableField("return_time")
    private Date returnTime;

    /**
     * 实际归还时间
     */
    @TableField("actual_return_time")
    private Date actualReturnTime;

    /**
     * 续借次数
     */
    @TableField("renew_count")
    private Integer renewCount;

    /**
     * 借阅状态：0-借阅中，1-已归还，2-已逾期未还，3-已逾期已还
     */
    private Integer status;

    /**
     * 罚款金额（元）
     */
    private Double fine;

    /**
     * 罚款状态：0-无需罚款，1-未缴纳，2-已缴纳
     */
    @TableField("fine_status")
    private Integer fineStatus;

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
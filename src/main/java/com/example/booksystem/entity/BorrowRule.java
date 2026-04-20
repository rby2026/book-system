package com.example.booksystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 借阅规则实体类
 */
@Data
@TableName("t_borrow_rule")
public class BorrowRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 借阅天数
     */
    @TableField("borrow_days")
    private Integer borrowDays;

    /**
     * 最大续借次数
     */
    @TableField("max_renew_count")
    private Integer maxRenewCount;

    /**
     * 每次续借天数
     */
    @TableField("renew_days")
    private Integer renewDays;

    /**
     * 最大借阅数量
     */
    @TableField("max_borrow_count")
    private Integer maxBorrowCount;

    /**
     * 逾期罚款（元/天）
     */
    @TableField("fine_per_day")
    private Double finePerDay;

    /**
     * 规则状态：0-禁用，1-启用
     */
    private Integer status;

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
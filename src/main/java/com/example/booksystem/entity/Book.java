package com.example.booksystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 图书实体类
 */
@Data
@TableName("t_book")
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图书ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 图书名称
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * ISBN编号
     */
    private String isbn;

    /**
     * 分类
     */
    private String category;

    /**
     * 子分类
     */
    @TableField("sub_category")
    private String subCategory;

    /**
     * 位置
     */
    private String location;

    /**
     * 图书状态：0-不可借阅，1-可借阅
     */
    private Integer status;

    /**
     * 图书简介
     */
    private String description;

    /**
     * 图书封面
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 总数量
     */
    @TableField("total_count")
    private Integer totalCount;

    /**
     * 可借数量
     */
    @TableField("available_count")
    private Integer availableCount;

    /**
     * 借阅次数
     */
    @TableField("borrow_count")
    private Integer borrowCount;

    /**
     * 出版日期
     */
    @TableField("publish_date")
    private Date publishDate;

    /**
     * 页数
     */
    private Integer pages;

    /**
     * 价格
     */
    private Double price;

    /**
     * 语言
     */
    private String language;

    /**
     * 版本
     */
    private String version;

    /**
     * 索书号
     */
    @TableField("call_number")
    private String callNumber;

    /**
     * 入库日期
     */
    @TableField("stock_date")
    private Date stockDate;

    /**
     * 最后借阅日期
     */
    @TableField("last_borrow_date")
    private Date lastBorrowDate;

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
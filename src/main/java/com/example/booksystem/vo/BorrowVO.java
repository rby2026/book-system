package com.example.booksystem.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 借阅VO
 */
@Data
public class BorrowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 借阅ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 图书ID
     */
    private Long bookId;

    /**
     * 图书名称
     */
    private String bookTitle;

    /**
     * 作者
     */
    private String bookAuthor;

    /**
     * ISBN
     */
    private String bookIsbn;

    /**
     * 借阅时间
     */
    private Date borrowTime;

    /**
     * 应还时间
     */
    private Date returnTime;

    /**
     * 实际归还时间
     */
    private Date actualReturnTime;

    /**
     * 续借次数
     */
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
    private Integer fineStatus;

    /**
     * 创建时间
     */
    private Date createTime;
}
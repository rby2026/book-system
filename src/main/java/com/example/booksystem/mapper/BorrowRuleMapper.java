package com.example.booksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.booksystem.entity.BorrowRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 借阅规则Mapper接口
 */
@Mapper
public interface BorrowRuleMapper extends BaseMapper<BorrowRule> {
}
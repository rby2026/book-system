package com.example.booksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.booksystem.entity.Borrow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 借阅Mapper接口
 */
@Mapper
public interface BorrowMapper extends BaseMapper<Borrow> {
}
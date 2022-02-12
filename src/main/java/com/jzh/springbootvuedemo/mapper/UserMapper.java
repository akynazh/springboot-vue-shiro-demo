package com.jzh.springbootvuedemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jzh.springbootvuedemo.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 处理User数据库操作
 * @author Jiang Zhihang
 * @date 2022/2/4 21:24
 */
public interface UserMapper extends BaseMapper<User> {
}

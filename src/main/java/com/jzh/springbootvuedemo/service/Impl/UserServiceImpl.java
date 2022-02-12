package com.jzh.springbootvuedemo.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzh.springbootvuedemo.entity.User;
import com.jzh.springbootvuedemo.mapper.UserMapper;
import com.jzh.springbootvuedemo.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @description
 * @Author Jiang Zhihang
 * @Date 2022/2/4 21:23
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}

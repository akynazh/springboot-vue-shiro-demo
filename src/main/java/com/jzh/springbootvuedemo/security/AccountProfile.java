package com.jzh.springbootvuedemo.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @version 1.0
 * @description
 * @Author Jiang Zhihang
 * @Date 2022/2/5 21:25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AccountProfile implements Serializable {
    private String email; // 用户登录名，也可为手机号等
    private String phone;
    private String name;
}

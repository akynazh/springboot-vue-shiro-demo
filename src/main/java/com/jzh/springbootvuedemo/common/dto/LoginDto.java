package com.jzh.springbootvuedemo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @version 1.0
 * @description 前端表单类
 * @Author Jiang Zhihang
 * @Date 2022/2/5 20:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginDto implements Serializable {
    @NotBlank(message = "请输入注册的邮箱")
    @Email(message = "邮箱格式错误")
    private String email;
    @NotBlank(message = "密码不能为空")
    private String password;
}

package com.jzh.springbootvuedemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @version 1.0
 * @description User实体类
 * @Author Jiang Zhihang
 * @Date 2022/2/4 20:57
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("t_user")
public class User implements Serializable {
    @TableId(value = "userId", type = IdType.AUTO)
    private Long userId;
    @NotBlank(message = "用户名不能为空")
    private String userName;
    @NotBlank(message = "密码不能为空")
    private String userPassword;
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    private String userEmail;
    private String userRole;
    private String userPermission;
}

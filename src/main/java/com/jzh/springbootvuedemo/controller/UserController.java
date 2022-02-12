package com.jzh.springbootvuedemo.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jzh.springbootvuedemo.common.dto.LoginDto;
import com.jzh.springbootvuedemo.common.response.RestfulResponse;
import com.jzh.springbootvuedemo.entity.User;
import com.jzh.springbootvuedemo.service.UserService;
import com.jzh.springbootvuedemo.utility.JwtUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @version 1.0
 * @description User控制器
 * @Author Jiang Zhihang
 * @Date 2022/2/4 22:12
 */
@RestController
public class UserController {
    final UserService userService;
    final JwtUtils jwtUtils;

    @Autowired
    public UserController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * @description: app初始界面
     * @author Jiang Zhihang
     * @date 2022/2/5 17:22
     */
    @GetMapping("/")
    public RestfulResponse init() {
        return RestfulResponse.success();
    }

    /**
     * @description: 前往admin初始页面
     * @author Jiang Zhihang
     * @date 2022/2/5 17:23
     */
    @RequiresAuthentication
    @RequiresRoles("admin")
    @GetMapping("/admin/index")
    public RestfulResponse admin_index() {
        return RestfulResponse.success();
    }

    /**
     * @description: 前往admin编辑页面
     * @author Jiang Zhihang
     * @date 2022/2/7 19:05
     */
    @RequiresAuthentication
    @RequiresRoles("admin")
    @RequiresPermissions(logical = Logical.AND, value = {"view", "edit"})
    @GetMapping("/admin/edit")
    public RestfulResponse admin_edit() {
        return RestfulResponse.success();
    }

    /**
     * @description: 前往student初始页面
     * @author Jiang Zhihang
     * @date 2022/2/5 17:23
     */
    @RequiresAuthentication
    @RequiresRoles("student")
    @GetMapping("/student/index")
    public RestfulResponse student_index() {
        return RestfulResponse.success();
    }

    /**
     * @description: 前往teacher初始页面
     * @author Jiang Zhihang
     * @date 2022/2/5 17:23
     */
    @RequiresAuthentication
    @RequiresRoles("teacher")
    @GetMapping("/teacher/index")
    public RestfulResponse teacher_index() {
        return RestfulResponse.success();
    }

    /**
     * @description:
     * 退出登录操作，前端要进行删除token操作并退回登录页面，后端删除认证信息
     * 注意：
     * 1. 如果token并没有过期，postman中前端在带着原来的token访问还是可以访问成功并生成新的认证信息
     * 2. 在一次session中，用户第一次登录成功只给前端返回token，前端每次发送请求都会在请求头中携带token，
     *    只要后端检查到token则会进行校验，校验成功则生成一次session中的认证信息。
     *    在这一次session中，后端保留了认证信息，这时前端即使不带token也可以访问成功，
     *    即可以成功通过@RequiresAuthentication, 但logout后删除了认证信息，不带token则访问失败
     * @author Jiang Zhihang
     * @date 2022/2/5 17:23
     */
    @GetMapping("/logout")
    public RestfulResponse logout() {
        SecurityUtils.getSubject().logout();
        return RestfulResponse.success();
    }

    /**
     * @description: 首次登录，无需认证，直接验证密码并生成token，并于请求头添加token
     * @author Jiang Zhihang
     * @date 2022/2/5 0:18
     */
    @PostMapping("/login")
    public RestfulResponse login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response) {
        User myUser = userService.getOne(new QueryWrapper<User>().eq("userEmail", loginDto.getEmail()));
        Assert.notNull(myUser, "用户不存在");
        if (!loginDto.getPassword().equals(myUser.getUserPassword())) {
            return RestfulResponse.fail(400, "密码错误");
        }
        // 密码正确则登录成功并设置token
        String token = jwtUtils.createToken(myUser.getUserEmail());
        response.setHeader("auth", token);
        response.setHeader("Access-Control-Expose-Headers", "auth");
        return RestfulResponse.success(MapUtil.builder()
                .put("userName", myUser.getUserName())
                .put("userEmail", myUser.getUserEmail())
                .put("userRole", myUser.getUserRole())
                .map()
        );
    }
}

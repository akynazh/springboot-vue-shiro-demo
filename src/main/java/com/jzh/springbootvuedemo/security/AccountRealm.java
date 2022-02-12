package com.jzh.springbootvuedemo.security;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jzh.springbootvuedemo.entity.User;
import com.jzh.springbootvuedemo.service.UserService;
import com.jzh.springbootvuedemo.utility.JwtUtils;
import io.jsonwebtoken.Claims;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @version 1.0
 * @description 用于向数据库查询验证账户
 * @Author Jiang Zhihang
 * @Date 2022/2/4 23:21
 */
@Component
public class AccountRealm extends AuthorizingRealm {
    final JwtUtils jwtUtils;
    final UserService userService;

    @Autowired
    public AccountRealm(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    /**
     * @description: 必须添加，设置支持于自己指定的JwtToken
     * @author Jiang Zhihang
     * @date 2022/2/5 0:31
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * @description: 认证校验
     * @author Jiang Zhihang
     * @date 2022/2/4 23:34
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        JwtToken jwtToken = (JwtToken) authenticationToken;
        Claims claims = jwtUtils.getClaimsByToken((String) jwtToken.getPrincipal());

        String email = claims.getSubject();
        User user = userService.getOne(new QueryWrapper<User>().eq("userEmail", email));
        if (user == null) {
            throw new UnknownAccountException("用户不存在");
        }
        // 所有对象统一为AccountProfile内置属性rid作为统一redis缓存id
        AccountProfile profile = new AccountProfile();
        profile.setEmail(user.getUserEmail());
        return new SimpleAuthenticationInfo(profile, jwtToken.getCredentials(), this.getClass().getName());
    }

    /**
     * @description: 权限校验
     * @author Jiang Zhihang
     * @date 2022/2/5 17:17
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        Object principal = principalCollection.getPrimaryPrincipal();
        // 这里从redis获取的信息不能被反序列化导致无法直接强转为AccountProfile
        AccountProfile profile = JSONUtil.parse(principal).toBean(AccountProfile.class);
        User user = userService.getOne(new QueryWrapper<User>().eq("userEmail", profile.getEmail()));
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.addRole(user.getUserRole());
        java.util.List<String> permissions = Arrays.asList(user.getUserPermission().split(","));
        simpleAuthorizationInfo.addStringPermissions(permissions);
        return simpleAuthorizationInfo;
    }
}

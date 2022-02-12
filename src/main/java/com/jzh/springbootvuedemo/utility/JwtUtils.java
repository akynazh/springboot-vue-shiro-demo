package com.jzh.springbootvuedemo.utility;

import io.jsonwebtoken.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @version 1.0
 * @description Jwt工具包
 * @Author Jiang Zhihang
 * @Date 2022/2/4 22:39
 */
@Data
@Component
@Slf4j
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtUtils {
    private String secret;
    private int expire;

    /**
     * @description: 生成token
     * @author Jiang Zhihang
     * @date 2022/2/4 22:50
     */
    public String createToken(String email) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + expire * 1000L); // 乘上1000ms
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(email)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * @description: 根据用户的token获取claims用于校验token
     * @author Jiang Zhihang
     * @date 2022/2/4 22:53
     */
    public Claims getClaimsByToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("token error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * @description: 校验token是否过期
     * @author Jiang Zhihang
     * @date 2022/2/4 23:09
     */
    public boolean isTokenExpired(Date expireDate) {
        return expireDate.before(new Date());
    }
}

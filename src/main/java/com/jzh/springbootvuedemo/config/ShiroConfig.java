package com.jzh.springbootvuedemo.config;

import com.jzh.springbootvuedemo.security.AccountRealm;
import com.jzh.springbootvuedemo.security.JwtFilter;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @description Shiro安全框架配置
 * @Author Jiang Zhihang
 * @Date 2022/2/4 22:56
 */
@Configuration
public class ShiroConfig {
    final JwtFilter jwtFilter;
    private int expire;

    @Autowired
    public ShiroConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * @description: session管理员集成redis
     * @author Jiang Zhihang
     * @date 2022/2/4 23:40
     */
    @Bean
    public SessionManager sessionManager(RedisSessionDAO redisSessionDAO) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionDAO(redisSessionDAO);
        return sessionManager;
    }

    /**
     * @description: 设置安全管理员
     * @author Jiang Zhihang
     * @date 2022/2/4 23:42
     */
    @Bean
    public DefaultWebSecurityManager securityManager(AccountRealm accountRealm,
                                                     SessionManager sessionManager,
                                                     RedisCacheManager redisCacheManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(accountRealm);
        securityManager.setSessionManager(sessionManager);
        // 必须要设置主键名称，shiro-redis 插件用过这个缓存用户信息
        redisCacheManager.setPrincipalIdFieldName("email");
        securityManager.setCacheManager(redisCacheManager);
        return securityManager;
    }

    /**
     * @description:
     在ShiroFilterChainDefinition中，我们不再通过编码形式拦截Controller访问路径，
     而是所有的路由都需要经过JwtFilter这个过滤器，然后判断请求头中是否含有jwt的信息，
     有就登录，没有就跳过。跳过之后，有Controller中的shiro注解进行再次拦截，比如@RequiresAuthentication，这样控制权限访问。
     * @author Jiang Zhihang
     * @date 2022/2/4 23:58
     */
    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        chainDefinition.addPathDefinition("/**", "jwt");
        chainDefinition.addPathDefinition("/static/**", "anon");
        return chainDefinition;
    }

    /**
     * @description: shiro工厂bean，设置安全管理员和jwt过滤器
     * @author Jiang Zhihang
     * @date 2022/2/4 23:59
     */
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(org.apache.shiro.mgt.SecurityManager securityManager,
                                                         ShiroFilterChainDefinition shiroFilterChainDefinition) {
        ShiroFilterFactoryBean shiroBean = new ShiroFilterFactoryBean();
        shiroBean.setSecurityManager(securityManager);

        Map<String, javax.servlet.Filter> filters = new HashMap<>();
        filters.put("jwt", jwtFilter);
        shiroBean.setFilters(filters);
        Map<String, String> filterChainMap = shiroFilterChainDefinition.getFilterChainMap();
        shiroBean.setFilterChainDefinitionMap(filterChainMap);

        return shiroBean;
    }
}

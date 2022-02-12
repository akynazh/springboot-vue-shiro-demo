# Spring-Vue-Demo

## 前言

SpringBoot整合Vue的一个Demo，同时后端整合了Shiro+Jwt+Redis的安全框架。

通过一个demo进一步了解了前后端分离的架构思想以及安全框架shiro的运行机制。

## 后端开发

### 主要工具

- SpringBoot
- MyBatisPlus
- Shiro
- Redis

### 新建数据库

```sql
CREATE TABLE `t_user` (
  `userId` bigint(20) NOT NULL AUTO_INCREMENT,
  `userName` varchar(255) NOT NULL,
  `userPassword` varchar(255) NOT NULL,
  `userEmail` varchar(255) NOT NULL,
  `userRole` varchar(255) NOT NULL,
  `userPermission` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
```

包括用户ID，用户名，用户密码，用户邮箱，用户角色，用户许可。

加入几个对象：

```
1,student,student,student@qq.com,student,view
2,teacher,teacher,teacher@qq.com,teacher,view
3,admin1,admin1,admin1@qq.com,admin,view
4,admin2,admin2,admin2@qq.com,admin,"view,edit"
```

包括学生，教师，只可以查看的管理员，可以查看和编辑的管理员。

### 新建springboot项目并引入依赖

```java
<!-- 自定义配置字段时通过build可以让springboot自动配置meta数据 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
</dependency>

<!-- redis + shiro-->
<dependency>
    <groupId>org.crazycake</groupId>
    <artifactId>shiro-redis-spring-boot-starter</artifactId>
    <version>3.2.1</version>
</dependency>
<!-- hutool工具类-->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>5.3.3</version>
</dependency>
<!-- jwt -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
<!--用于@Notblank等注解-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!--springboot-web开发-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<!--mybatis-plus-->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.1</version>
</dependency>
<!--devtool工具-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
<!--mysql连接-->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
<!--@Data等注解使用-->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
<!--springboot-test使用-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
    <exclusions>
        <exclusion>
            <groupId>org.junit.vintage</groupId>
            <artifactId>junit-vintage-engine</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 编辑application.yaml

```yaml
spring:
  application:
    name: springboot-vue-demo
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/test?serverTimezone=UTC
    username: root
    password: 123456
  redis:
    port: 6379
    jedis:
      pool:
        max-active: 1000
        max-wait: -1
        min-idle: 5
        max-idle: 10
    timeout: 6000
server:
  port: 8080

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: false
```

### 创建实体类

```java
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
```

### 创建实体类对应mapper层和service层

```java
public interface UserService extends IService<User> {}
```

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
```

```java
public interface UserMapper extends BaseMapper<User> {
}
```

### 创建Mybatis配置类

添加mapper扫描路径以及开启事务

```java
/**
 * @version 1.0
 * @description MyBatisPlus配置
 * @Author Jiang Zhihang
 * @Date 2022/2/4 21:26
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.jzh.springbootvuedemo.mapper")
public class MbpConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        return interceptor;
    }

    public OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor() {
        return new OptimisticLockerInnerInterceptor();
    }
}
```

### 创建Jwt工具类

在这之前在application.yaml中配置自己的Jwt密钥和过期时间：

```yaml
auth:
  jwt:
    secret: dasfjlsajdlkeeawrljawr213jfsad
    expire: 43200
```

注意expire以秒为单位，new Date() 时以毫秒为单位。

工具类作用如下：

- 生成token
- 根据用户的token获取claims用于校验token
- 校验token是否过期

```java
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
```

### 创建JwtToken类

JwtToken继承于Shiro包下的AuthenticationToken，用于Shiro认证。

```java
/**
 * @version 1.0
 * @description token类
 * @Author Jiang Zhihang
 * @Date 2022/2/4 22:57
 */
public class JwtToken implements AuthenticationToken {
    private final String token;

    public JwtToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
```

### 创建AccountProfile类

用于Shiro认证存储的对象。

```java
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
    
    // 可选项
    private String phone;
    private String name;
}
```

这里提前提醒一下必须在后续注入RedisManager时指定AccountProfile类用于Redis缓存的key字段！

### 创建AccountRealm类

用于向数据库查询验证账户以及权限校验信息的注册。

```java
/**
 * @version 1.0
 * @description 用于向数据库查询验证账户以及权限校验信息的注册
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
```

### 创建Jwt过滤器

过滤流程：

1. 首先经过prehandle，用于支持跨域请求。

2. 接着经过onAccessDenied，拦截请求Header中是否有auth字段并进行对应操作。

3. 如果没有token，则直接通过，后续通过@RequiresAuthentication等进行拦截。

4. 如果有token，则校验token失效，如果失效则通过失败执行onLoginFailure。

5. 如果token未失效，则通过Shiro进行登录认证以及可选的权限校验。

```java
/**
 * @version 1.0
 * @description jwt过滤器
 * @Author Jiang Zhihang
 * @Date 2022/2/4 22:59
 */
@Component
public class JwtFilter extends AuthenticatingFilter {
    private JwtUtils jwtUtils;

    @Autowired
    public void setJwtUtils(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * @description: 拦截用户请求
     * @author Jiang Zhihang
     * @date 2022/2/5 0:28
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = request.getHeader("auth");
        // 如果没有token，则直接通过，后续通过@RequiresAuthentication等进行拦截
        if (StringUtils.isEmpty(token)) {
            return true;
        } else { // 如果有token，则进行校验
            // 校验token
            Claims claims = jwtUtils.getClaimsByToken(token);
            if (claims == null || jwtUtils.isTokenExpired(claims.getExpiration())) {
                throw new ExpiredCredentialsException("token已经失效，请重新登录");
            }
            // 继续继续校验，校验成功则登录成功
            return executeLogin(servletRequest, servletResponse);
        }
    }

    /**
     * @description: 把请求头中的token生成到JwtToken类中
     * @author Jiang Zhihang
     * @date 2022/2/5 0:28
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = request.getHeader("auth");
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        return new JwtToken(token);
    }

    /**
     * @description: 登录失败返回错误消息
     * @author Jiang Zhihang
     * @date 2022/2/5 17:25
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        Throwable throwable = e.getCause() == null ? e : e.getCause();
        RestfulResponse restfulResponse = RestfulResponse.fail(400, throwable.getMessage());
        String JsonStr = JSONUtil.toJsonStr(restfulResponse);
        try {
            httpServletResponse.getWriter().print(JsonStr);
        } catch (IOException ignored){

        }
        return false;
    }

    /**
     * @description: 用于支持跨域
     * @author Jiang Zhihang
     * @date 2022/2/5 0:30
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-Control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        /*
        假如：浏览器地址栏的地址是：http://localhost:4200/#/pages/dashadmin
        异步请求后端地址：http://localhost:8080/secure
        请求头：
        ...
        Host:localhost:8080
        Origin:http://localhost:4200
        Referer:http://localhost:4200/
        ...
        从上面的请求头中可以看到request.getHeader("Origin")的值就是就是http://localhost:4200
        request.getHeader("Origin")经常用法就是用来解决浏览器跨域问题
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        指的是只允许http://localhost:4200跨域访问后端服务器。
        */
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");

        /*
        响应首部 Access-Control-Allow-Headers 用于 preflight request （预检请求）中，
        列出了将会在正式请求的 Access-Control-Expose-Headers 字段中出现的首部信息。修改为请求首部
         */
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));

        // 跨域时会首先发送一个OPTIONS请求，这里我们给OPTIONS请求直接返回正常状态
        /*
        options 请求就是预检请求，可用于检测服务器允许的http 方法。
        当发起跨域请求时，由于安全原因，触发一定条件时浏览器会在正式请求之前自动先发起OPTIONS 请求，即CORS 预检请求，
        服务器若接受该跨域请求，浏览器才继续发起正式请求。
         */
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(org.springframework.http.HttpStatus.OK.value());
            return false;
        }

        return super.preHandle(request, response);
    }
}
```
### 创建Shiro配置类

这里通过@Bean，外部可以import我们自己配置的东西。

1. 配置SessionManager

注入RedisSessionDAO，session管理员集成redis。

2. 配置DefaultWebSecurityManager

注入AccountRealm， SessionManager，RedisCacheManager。

其中必须记得要设置主键名称，shiro-redis 插件用过这个缓存用户信息：

```java
redisCacheManager.setPrincipalIdFieldName("email");
```

3. 配置ShiroFilterChainDefinition

在ShiroFilterChainDefinition中，我们不再通过编码形式拦截Controller访问路径， 

而是所有的路由都需要经过JwtFilter这个过滤器，然后判断请求头中是否含有jwt的信息，

有就登录，没有就跳过。跳过之后，有Controller中的shiro注解进行再次拦截，比如@RequiresAuthentication，这样控制权限访问。

4. 配置ShiroFilterFactoryBean

shiro工厂bean，设置安全管理员和jwt过滤器。

```java
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

```

### 新建跨域请求配置类

```java
/**
 * @version 1.0
 * @description 跨域请求配置
 * @Author Jiang Zhihang
 * @Date 2022/2/5 0:33
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*");
    }
}
```

### 新建全局Restful风格型返回数据

```java
/**
 * @version 1.0
 * @description 前后端交互数据
 * @Author Jiang Zhihang
 * @Date 2022/2/4 22:02
 */
@Data
public class RestfulResponse implements Serializable {
    private int code;
    private String message;
    private Object data;

    public static RestfulResponse response(int code, String message, Object data) {
        RestfulResponse response = new RestfulResponse();
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static RestfulResponse success(Object data) {
        return response(200, "操作成功", data);
    }

    public static RestfulResponse success() {
        return response(200, "操作成功", null);
    }

    public static RestfulResponse fail(int code, String message) {
        return response(code, message, null);
    }
}
```

### 新建全局异常处理类

全局处理异常，方便异常消息的捕获和向前端反馈信息。

```java
/**
 * @version 1.0
 * @description
 * @Author Jiang Zhihang
 * @Date 2022/2/4 22:01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ShiroException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public RestfulResponse handler(ShiroException e) {
        log.error("身份验证异常: {}", e.getMessage());
        return RestfulResponse.fail(401, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestfulResponse handler(MethodArgumentNotValidException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        StringBuilder sb = new StringBuilder();
        for (ObjectError error : allErrors) {
            sb.append(error).append("&");
        }
        log.error("实体校验异常: {}", e.getMessage());
        return RestfulResponse.fail(400, sb.toString());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestfulResponse handler(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage());
        return RestfulResponse.fail(400, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestfulResponse handler(IllegalArgumentException e) {
        log.error("Assert异常: {}", e.getMessage());
        return RestfulResponse.fail(400, e.getMessage());
    }
}
```

### 新建前端登录表单类

```java
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
```

### 新建User控制器

注：

1. @RequiresAuthentication代表通过过滤器后需要进一步进行认证。

2. @RequiresRoles("admin")代表需要有admin对象才可进行访问。

3. @RequiresPermissions(logical = Logical.AND, value = {"view", "edit"})代表必须同时拥有view和edit两个权限可访问。

4. 对于退出登录操作的解释：

退出登录操作，前端要进行删除token操作并退回登录页面，后端删除认证信息。

必须注意，如果token并没有过期，postman中前端在带着原来的token访问还是可以访问成功并生成新的认证信息。 

在一次session中，用户第一次登录成功只给前端返回token，前端每次发送请求都会在请求头中携带token。

只要后端检查到请求头中的token则会进行校验，校验成功则生成一次session中的认证信息。

在这一次session中，后端保留了认证信息，这时前端即使不带token也可以访问成功，即可以成功通过@RequiresAuthentication。

但logout后删除了认证信息，不带token则访问失败。

```java
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
```

### 运行项目

运行在8080端口，后续的前端项目则运行在8081端口。

### 通过Postman调试

注意post方法：

1. header中添加content-type: application/json
2. body设置为raw并添加json数据

## 前端开发

### 主要工具

- vue2
- element-ui
- axios

### 新建项目，导入依赖

通过```vue ui```快速新建项目，完成后通过```npm install```导入axios，element等组件。

### 创建页面和组件

- Home.vue： 主页面

- Index.vue： 登录成功页面

- Login.vue： 登录页面

- Footer.vue： 公共尾部

- Header.vue： 公共头部

### 配置router

src/router/index.js

meta.auth数据用于判断进入页面是否需要认证。

```js
import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login'
import Index from '../views/Index'
Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    component: Home,
  },
  {
    path: '/login',
    component: Login,
  },
  {
    path: '/index',
    component: Index,
    meta: {
      auth: true
    }
  }
]

const router = new VueRouter({
  routes
})

export default router
```

### 配置store

更方便地更新和存储数据。

src/store/index.js

```js
import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
    state: {
        token: localStorage.getItem("token"),
        user: JSON.parse(sessionStorage.getItem("user"))
    },
    mutations: {
        SET_TOKEN: (state, token) => {
            state.token = token
            localStorage.setItem("token", token)
        },
        SET_USER: (state, user) => {
            state.user = user
            sessionStorage.setItem("user", JSON.stringify(user))
        },
        REMOVE_INFO: state => {
            state.token = null
            state.user = null
            localStorage.setItem("token", null)
            sessionStorage.setItem("user", null)
        }
    },
    getters: {
        getUser: state => {
            return state.user
        }
    }
})
```

### 定义axios拦截器

1. 由于前后端分离，项目部署在不同端口上，所以设置所有axios访问后端的baseURL为后端的地址。

2. 所有axios请求结果都先通过一个方法拦截判断成功与否，失败则reject终止并反馈信息，成功则resolve并放行，减少了许多代码量。

src/main.js

```js
// axios请求前置拦截
axios.defaults.baseURL = "http://localhost:8080"
axios.interceptors.response.use(
    response => {
        if (response.status === 200) {
            console.log(response.data)
            return Promise.resolve(response)
        } else {
            return Promise.reject(response)
        }
    },
    error => {
        let response = error.response
        Message.error({
            message: response.data.message,
            duration: 1500,
        })
        return Promise.reject(response)
    }
)


```

### 定义router拦截器

前后端分离，前端则必须定义自己路由的拦截器，实现资源的合理控制。

通过src/router/index.js中meta下的字段即可进行合理的操控了，比如通过auth真假与否决定一个访问静态页面资源是否需要认证。

```js
// router前置拦截
router.beforeEach((to, from, next) => {
    if (to.matched.some(record => record.meta.auth)) {
        const token = localStorage.getItem("token")
        if (token) {
            next()
        } else {
            Element.Message({
                message: "请先登录哦",
                duration: 1000
            })
            next({
                path: '/login'
            })
        }
    } else {
        next()
    }
})
```

### 部署调试

通过```npm run serve```即可进行预览。

可在vue.config.js中配置预览端口地址：

```js
module.exports = {
    devServer: {
        port: 8081
    }
}
```

### 正式部署

1. 通过```npm run build```部署并在dist中。

2. 接着安装serve。

```
npm install -g serve
```

3. 将dist中项目部署在8081端口。

暂时部署：
```
cd dist
serve -p 8081
```

永久部署：
```
cd dist
nohup serve -p 8081  &
```

### 与后端进行交互

1. 登录界面

![](./preview-cut/1.png)

2. 登录成功界面

![](./preview-cut/2.png)

3. admin2访问成功admin-index

![](./preview-cut/3.png)

4. admin1访问失败student

![](./preview-cut/4.png)

5. admin1访问失败admin-edit

![](./preview-cut/5.png)

## 总结

通过一个demo进一步了解了前后端分离的架构思想以及安全框架shiro的运行机制。

前后端分离很重要，让前后端都更专注于自己该干的活。
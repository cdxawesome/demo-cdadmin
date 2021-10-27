代码地址:[https://github.com/cdxawesome/demo-cdadmin](https://github.com/cdxawesome/demo-cdadmin)  

参考项目地址:[https://github.com/elunez/eladmin](https://github.com/elunez/eladmin)  

**此demo参考eladmin项目，加上一些自己的理解做的，仅供做学习使用。**  

### 1.技术栈

此项目使用SpringSecurity+JWT的方式，实现基于角色的权限控制(RBAC思想)。

使用的技术栈：

- SpringBoot
- SpringSecurity
- JWT
- MyBatis-Plus
- Knife4j
- Druid
- MySQL
- Easy-Captcha
- Redis
- Vue
- Axios



### 2.实现效果

每个用户对应一个或多个角色，每个角色对应多个用户，是多对多的关系。在Controller的接口上，进行角色判断，用户拥有对应的角色即可访问该接口。

### 3.开发步骤

#### 3.1数据库表实体类和mapper接口

根据数据库表创建对应的pojo，然后定义对应的mapper接口继承`BaseMapper<>`。准备好之后测试数据库的使用是否正常。

#### 3.2实现验证码功能

##### 3.2.1 配置文件映射到实体类

在`application.yaml`配置文件中，添加自定义配置，然后将配置映射到实体类中，我们在使用时，直接通过实体类的Bean来获取配置即可。

```java
/**
 * 从配置文件中读入配置转成pojo类
 */
@Configuration
public class ConfigBeanConfiguration {


    @Bean
    @ConfigurationProperties(prefix = "login")
    public LoginProperties loginProperties() {
        return new LoginProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "jwt")
    public SecurityProperties securityProperties() {
        return new SecurityProperties();
    }
}
```

将对应的配置，放在yaml文件中，要匹配前缀。

```yaml
jwt:
  header: Authorization
  token-start-with: Bearer
  # 令牌过期时间(秒)
  token-validity-in-seconds: 30
  online-key: online-token-
  # token可续期时间范围(秒),表示在这个范围内，如果用户还在访问页面，就给token续期 3600000
  renewal: 20
```

实体类代码可以自行查看。

##### 3.2.2 获取验证码

这里用的是easy-captcha这个插件，插件地址:https://github.com/pig-mesh/easy-captcha

```java
public ResultVo captchaCode() {
        // 获取captcha生成类
        Captcha captcha = loginProperties.getCaptcha();
        String uuid = UUID.randomUUID().toString();
        // 获取验证码的结果值
        String captchaValue = captcha.text();
        // 获取验证码图片的url
        String captchaUrl = captcha.toBase64();

        // 将验证码保存进redis中，并设置过期时间
        redisUtils.set(uuid, captchaValue, loginProperties.getLoginCode().getExpiration());

        // 封装验证码信息
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("uuid", uuid);
        resultMap.put("captchaUrl", captchaUrl);
        resultMap.put("captchaWidth", loginProperties.getLoginCode().getWidth());
        resultMap.put("captchaHeight", loginProperties.getLoginCode().getHeight());
        return new ResultVo(10000, "success", resultMap);
    }
```

这里用到的自定义结果类，就不做介绍了，比较简单，可以自行查看代码。

实现结果如下：

![image-20211027085759136](https://note-pic-sync.oss-cn-guangzhou.aliyuncs.com/img/image-20211027085759136.png)

#### 3.3 登陆功能

```java
public ResultVo login(LoginData loginData, HttpServletRequest request) {
        // 1.验证码校验
        // 从redis中获取验证码
        try {
            String captchaValue = (String) redisUtils.get(loginData.getUuid());
            if (captchaValue.equalsIgnoreCase(loginData.getCaptchaValue())) {
                // 2.用户名校验，从数据库查询数据
                User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", loginData.getUsername()));
                if (user != null) {
                    // 3.密码校验
                    // 先对密码进行rsa解密
                    String rawPassword = RsaUtils.decryptByPrivateKey(rsaProperties.getPrivateKey(), loginData.getPassword());
                    if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                        // 将redis中的验证码删除
                        redisUtils.del(loginData.getUuid());
                        // 生成token，保存在线用户信息，并且把token和用户信息封装响应给前端
                        String token = tokenProvider.getToken(user);
                        OnlineUser onlineUser = onlineUserService.save(user, token, request);
                        // TODO 判断该用户是否已经登陆了，如果已经登陆则不需要再次登陆，直接跳转到首页
                        // 因为这里做的是单用户的登陆，所以再次登录的话要先把之前的认证清除
                        tokenProvider.clearAuthentication();
                        // 添加认证
                        tokenProvider.doAuthentication(onlineUser);
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("user", user);
                        resultMap.put("token", token);
                        // 4.响应数据
                        return new ResultVo(10000, "登陆成功", resultMap);
                    } else {
                        return new ResultVo(10001, "用户名或密码错误", null);
                    }
                } else {
                    return new ResultVo(10001, "用户名或密码错误", null);
                }
            } else {
                return new ResultVo(10001, "验证码错误", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultVo(10001, "验证码已过期,点击验证码刷新", null);
        }
    }
```

##### 3.3.1 验证码和密码校验

首先对验证码进行校验。

从数据库中查找用户名，对用户名进行校验。

密码校验：前端传过来的密码是经过RSA加密的，而我们存储在数据库中的密码，可以通过RSA加密，也可以用SpringSecurity的`PasswordEncoder`来加密。这里密码比较的时候，是将数据库的密码，前端传回的密码，都解密成明文进行比较的。

##### 3.3.2 生成token

> jwt的官网列出了java的多种实现库，这里使用的是java-jwt

```java
/**
* 生成token
*
* @param user 传入user对象
* @return 返回token的字符串
*/
public String getToken(User user) {
    Algorithm algorithm = Algorithm.HMAC256("simple199486");
    String token = JWT.create()
        .withIssuer("simple")
        .withClaim("username", user.getUsername())
        .withIssuedAt(new Date())
        .withSubject("Login Authorization")
        .withJWTId(UUID.randomUUID() + "")
        .sign(algorithm);
    return token;
}
```

##### 3.3.3 保存在线用户信息

定义了一个类来表示在线用户的信息，并且定义一个service，将在线用户的token存储到redis中

##### 3.3.4 添加系统认证

> 因为这里我没有使用SpringSecurity的登陆逻辑，而是自定义了登陆接口。当使用SpringSecurity自带的登陆逻辑时，需要去实现UserDetailsService接口，并且重写loadUserByUsername方法，登陆成功后，系统就会给该用户添加认证。我们这里需要手动给系统添加认证信息。

```java
  /**
     * 添加认证信息到SpringSecurity中
     *
     * @param onlineUser 在线用户
     */
    public void doAuthentication(OnlineUser onlineUser) {
        // 添加此用户的认证到系统中
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        User_Role userRole = userRoleMapper.selectOne(new QueryWrapper<User_Role>().eq("user_id", onlineUser.getUserId()));
        // 查询角色表
        Role role = roleService.findRoleById(userRole.getRoleId());
        // 将用户权限数据(用户角色)添加进系统内
        List<GrantedAuthority> authorityList = AuthorityUtils.createAuthorityList("ROLE_" + role.getRoleName());
        UsernamePasswordAuthenticationToken authenticationToken = new
                UsernamePasswordAuthenticationToken(onlineUser.getUsername(), onlineUser.getToken(), authorityList);
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
    }
```

##### 3.3.5 响应数据

将登陆用户的信息，token响应给前端。前端将token存储在cookie中，下次访问时，需把token放在header中发送请求。

#### 3.4 权限验证

当用户登陆之后，得到了系统的认证，并且得到一个token。这里的系统认证，是存储在`SecurityContextHolder`里面的。目前做的是单用户的访问，没有做多用户的登陆，那么我可以认为`SecurityContextHolder`只有一个，且在系统内任意地方都可以访问，里面包含了用户登陆的信息以及权限信息(用户角色)。

当用户需要访问其他受保护资源时，验证过程如下：

##### 3.4.1 SpringSecurity自定义配置

自定义配置需要继承`WebSecurityConfigurerAdapter`,并重写方法

```java
@Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()
                .loginPage("/login.html");
        http.authorizeRequests()
                .antMatchers("/user/login", "/user/captchaCode", "/doc.html", "/swagger/**", "/login.html").permitAll()
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/v2/**").permitAll()
                .antMatchers("/favicon.ico/**").permitAll()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/user/test").permitAll()
                .anyRequest().authenticated()
                .and()
                .csrf().disable();
        http.exceptionHandling().accessDeniedHandler(myAccessDeniedHandler);
        http.addFilterBefore(new TokenFilter(securityProperties, onlineUserService, tokenProvider), UsernamePasswordAuthenticationFilter.class);
    }
```

在配置中，自定义了登陆界面、放行规则、前置过滤器

> 当用户的系统认证不通过时，就会自动跳转到登陆页面

##### 3.4.2 权限验证

用户访问受保护资源时，首先系统会判断该用户是否已经的登陆。如果没有登陆，会自动跳转到登陆页面。

如果已经登陆，则会进入到自定义的过滤器，判断token

###### 过滤器

```java
@Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        // 获取请求的uri
        String requestURI = httpServletRequest.getRequestURI();
        // 下列uri直接放行
        if (requestURI.contains(".js") || requestURI.contains("/captchaCode")
                || requestURI.contains("/error") || requestURI.contains("/login")
                || requestURI.contains(".html")) {
            chain.doFilter(request, response);
        } else {
            // 从request中获取到token
            String token = checkToken(httpServletRequest);
            // 校验token(token有值，说明是已经登陆了的)
            if (StringUtils.hasText(token)) {
                // 使用token查找在线用户
                OnlineUser onlineUser = onlineUserService.getOne(token);
                if (onlineUser != null) {
                    // 添加认证
                    tokenProvider.doAuthentication(onlineUser);
                    // token续期
                    tokenProvider.tokenRenewal(token);
                    chain.doFilter(request, response);
                } else {
                    //token校验失败，则清除当前用户的认证信息，需要重新登陆
                    tokenProvider.clearAuthentication();
                    // 放行
                    chain.doFilter(request, response);
                }
            } else {
                // 如果没有携带token，说明是匿名访问(没有登陆)，直接放行
                chain.doFilter(request, response);
            }
        }

    }
```

token验证通过后，对token进行续期。如果验证不通过，说明token不合法或者是token过期，直接清除用户的系统认证，让其重新登陆。

```java
/**
* 清除系统的认证
*/
public void clearAuthentication() {
    SecurityContextHolder.clearContext();
}
```

**需要注意的是，此demo中我使用了自定义的过滤器来校验token。在SpringSecurity中，有一个RememberMe功能。系统会自动生成token，并且会自行判断用户的token。我们也可以自定义token的存储媒介，使其存储在redis中，token也可以自定义使用jwt去生成。这就是另一种做法了**

###### 角色控制

我们在数据库中有角色表、用户表、用户角色表(第三个关系表)，在之前的添加系统认证的代码中，已经将用户的角色，映射到SpringSecurity的角色权限控制中。在对应接口上，只需要加上注解即可。

```java
@PreAuthorize("hasRole('normal')")
@GetMapping("/queryAll")
@ApiOperation(value = "查询所有用户")
public ResultVo queryAll() {
    return userService.queryAll();
}
```

 表示需要拥有"normal"角色的用户才可以访问，否则页面会显示403错误(无权访问)。这个注解需要在启动类上开启才可以使用。

```java
@SpringBootApplication
@MapperScan("com.simple.democdadmin.mapper")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AppRun {
    public static void main(String[] args) {
        SpringApplication.run(AppRun.class, args);
    }
}
```



---

附一张简单流程图

![image-20211027095521034](https://note-pic-sync.oss-cn-guangzhou.aliyuncs.com/img/image-20211027095521034.png)

### 4.问题(难点)记录以及TODO事项

#### 4.1 遇到的问题

- 用户的token过期了，但是仍然是登陆状态

  因为SpringSecurity存储了用户的认证信息，token过期之后，需要清除用户的认证信息。如果使用的是SpringSecurity的RememberMe功能，token过期后，系统会自动清除用户的认证信息。

- SpringSecurity整个权限验证过程的理解

  对这个过程理解清晰了，才能写好代码。还需要在看看官方文档。

- 数据库的角色表和SpringSecurity的角色权限关联起来

  使用SecurityContextHolder给用户添加权限列表

- SpringSecurity过滤器的理解

  Filter的初始化在Servlet之前，Bean的初始化在servlet之后。所以我们在Filter不能使用没有初始化的Bean对象。在这个demo中，把自定义的Filter的初始化交给构造函数来执行，我们在调用这个Filter的时候手动给其初始化，这个就可以在Filter中使用Bean了。

  另外，使用过滤器比较麻烦的一点是，所有的请求都会经过这个过滤器，然后我们只需要拦截需要验证的请求，其他请求都放行。这样下来配置放行规则就比较麻烦。

- redis的存取操作

  从redis中存储和读取数据需要指定一个key，在此demo中，token的存储key和前端传过来的token的开头字母，经常容易搞混。请求头中的token是需要加上`Bearer`的，如`Bearer xxxxx`。

- 配置信息从配置文件映射到pojo类

  pojo类可以交给Spring管理，我们就可以直接注入Bean来使用，非常方便。在需要修改配置时，只需要修改`applicaition.yaml`文件就可以了。

- SpringSecurity的PasswordEncoder加密和RSA加密，jwt的加密规则

  理解RSA加密，对称加密和非对称加密，公钥私钥的用法。jwt的HS256加密算法，盐。PasswordEncoder的BCryptPasswordEncoder加密等。

#### 4.2 TODO

1. SpringSecurity+Oauth2+JWT实现权限管理

   Oauth2使用授权模式或者密码模式，系统内有资源服务器和授权服务器

2. 实现多用户登陆管理

   实现多个用户账户可以登陆。用户账号抢登强制下线。前端显示所有在线用户。

3. 使用SpringSecurity的RememberMe功能实现token的存储和认证

package com.simple.democdadmin.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simple.democdadmin.config.bean.SecurityProperties;
import com.simple.democdadmin.mapper.UserRoleMapper;
import com.simple.democdadmin.service.RoleService;
import com.simple.democdadmin.vo.OnlineUser;
import com.simple.democdadmin.vo.Role;
import com.simple.democdadmin.vo.User;
import com.simple.democdadmin.vo.User_Role;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class TokenProvider {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private SecurityProperties securityProperties;

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

    /**
     * 添加认证信息到SpringSecurity中
     *
     * @param onlineUser 在线用户
     */
    public void doAuthentication(OnlineUser onlineUser) {
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

    /**
     * 清除系统的认证
     */
    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    /**
     * token续期
     *
     * @param token 传入一个token
     */
    public void tokenRenewal(String token) {
        Long expire = redisUtils.getExpireTime(securityProperties.getOnlineKey() + token);
        if (expire <= securityProperties.getRenewal()) {
            // 重置token过期时间，设置为token可续期时间
            redisUtils.setDuration(securityProperties.getOnlineKey() + token, securityProperties.getRenewal());
        }
    }
}

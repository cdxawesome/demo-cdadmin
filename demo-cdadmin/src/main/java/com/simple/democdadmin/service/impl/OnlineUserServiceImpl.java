package com.simple.democdadmin.service.impl;

import com.simple.democdadmin.config.bean.SecurityProperties;
import com.simple.democdadmin.service.OnlineUserService;
import com.simple.democdadmin.utils.RedisUtils;
import com.simple.democdadmin.vo.OnlineUser;
import com.simple.democdadmin.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
public class OnlineUserServiceImpl implements OnlineUserService {

    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 保存当前登陆用户
     * @param user 用户
     * @param token 携带的token
     * @param request 请求
     * @return 在线用户
     */
    @Override
    public OnlineUser save(User user, String token, HttpServletRequest request) {
        OnlineUser onlineUser = new OnlineUser();
        // 将登陆用户信息设置到在线用户中
        onlineUser.setUserId(user.getUserId());
        onlineUser.setUsername(user.getUsername());
        onlineUser.setLoginTime(new Date());
        onlineUser.setToken(token);
        // 存储token
        redisUtils.set(securityProperties.getOnlineKey() + token,
                onlineUser, securityProperties.getTokenValidityInSeconds());
        return onlineUser;
    }

    /**
     * 获取一个当前用户
     *
     * @param token 传入token
     * @return 返回当前在线用户
     */
    @Override
    public OnlineUser getOne(String token) {
        try {
            return (OnlineUser) redisUtils.get(securityProperties.getOnlineKey() + token);
        } catch (Exception e) {
            System.out.println("---------------redis中查询不到此用户----------------");
        }
        return null;
    }
}

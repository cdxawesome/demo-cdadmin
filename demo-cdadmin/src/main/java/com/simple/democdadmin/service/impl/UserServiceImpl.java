package com.simple.democdadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pig4cloud.captcha.base.Captcha;
import com.simple.democdadmin.config.bean.LoginProperties;
import com.simple.democdadmin.config.bean.RsaProperties;
import com.simple.democdadmin.mapper.UserMapper;
import com.simple.democdadmin.mapper.UserRoleMapper;
import com.simple.democdadmin.service.OnlineUserService;
import com.simple.democdadmin.service.UserService;
import com.simple.democdadmin.utils.RedisUtils;
import com.simple.democdadmin.utils.RsaUtils;
import com.simple.democdadmin.utils.TokenProvider;
import com.simple.democdadmin.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private LoginProperties loginProperties;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RsaProperties rsaProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private OnlineUserService onlineUserService;

    @Override
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

    @Override
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
            return new ResultVo(10001, "验证码已过期,点击验证码刷新", null);
        }
    }

    @Override
    public ResultVo add(User user, Long roleId) {
        // 将新增用户添加进数据库
        int insert = userMapper.insert(user);
        if (insert != 1) {
            return new ResultVo(10001, "用户添加失败", null);
        }
        User user1 = userMapper.selectOne(new QueryWrapper<User>().eq("username", user.getUsername()));
        // 将用户id和角色id，存进两者的关系表中
        userRoleMapper.insert(new User_Role(user1.getUserId(), roleId));
        return new ResultVo(10000, "success", null);
    }

    @Override
    public ResultVo queryAll() {
        List<User> users = userMapper.selectList(null);
        if (users.size() > 0) {
            return new ResultVo(10000, "success", users);
        }
        return new ResultVo(10001, "查询失败", null);
    }

    @Override
    public ResultVo test() {
        return new ResultVo(10000, "success", null);
    }


}

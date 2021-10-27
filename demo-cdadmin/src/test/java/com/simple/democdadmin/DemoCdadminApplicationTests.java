package com.simple.democdadmin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simple.democdadmin.config.bean.SecurityProperties;
import com.simple.democdadmin.mapper.UserMapper;
import com.simple.democdadmin.mapper.UserRoleMapper;
import com.simple.democdadmin.service.UserService;
import com.simple.democdadmin.utils.RsaUtils;
import com.simple.democdadmin.vo.User;
import com.simple.democdadmin.vo.User_Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;


@SpringBootTest
public class DemoCdadminApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityProperties securityProperties;

    @Value("${rsa.public_key}")
    private String public_key;
    @Value("${rsa.private_key}")
    private String private_key;

    @Test
    public void testMysql() {
        List<User> users = userMapper.selectList(null);
        System.out.println(users);
    }

    @Test
    public void select1() {
        User admin = userMapper.selectOne(new QueryWrapper<User>().eq("username", "admin"));
        System.out.println(admin);
    }

    @Test
    public void rsaEncrypt() throws Exception {
        String encryptPwd = RsaUtils.encryptByPublicKey(public_key, "123456");
        System.out.println(encryptPwd);
        String s = RsaUtils.decryptByPrivateKey(private_key, encryptPwd);
        System.out.println(s);
    }

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUserId(null);
        user.setDeptId(2L);
        user.setUsername("test1");
        user.setNickName("测试");
        user.setGender("男");
        user.setPhone("18900001111");
        user.setEmail("123123@foxmail.com");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setAdmin(false);


        userService.add(user, 2L);
    }

    @Test
    public void testUserRole() {
        int insert = userRoleMapper.insert(new User_Role(3L, 2L));
    }

    @Test
    public void testSecurityContextHolder() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        System.out.println(authentication.getName());

    }

    @Test
    public void testSecurityProperties() {
        String header = securityProperties.getHeader();
        System.out.println(System.currentTimeMillis());
        System.out.println(header);
    }

}

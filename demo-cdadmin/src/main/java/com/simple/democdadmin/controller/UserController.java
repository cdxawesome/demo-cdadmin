package com.simple.democdadmin.controller;

import com.simple.democdadmin.service.UserService;
import com.simple.democdadmin.vo.LoginData;
import com.simple.democdadmin.vo.ResultVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
@CrossOrigin
@Api(tags = "用户管理")
public class UserController {
    @Autowired
    private UserService userService;


    @ApiOperation(value = "获取验证码")
    @GetMapping("/captchaCode")
    public ResultVo captchaCode() {
        return userService.captchaCode();
    }

    @ApiOperation(value = "用户登陆")
    @ApiImplicitParam(name = "loginData", value = "登陆数据实体类", required = true)
    @PostMapping("/login")
    public ResultVo login(@RequestBody LoginData loginData, HttpServletRequest request) {
        return userService.login(loginData, request);
    }

    @PreAuthorize("hasRole('normal')")
    @GetMapping("/queryAll")
    @ApiOperation(value = "查询所有用户")
    public ResultVo queryAll() {
        return userService.queryAll();
    }

    @GetMapping("/test")
    public ResultVo test() {
        return userService.test();
    }

}

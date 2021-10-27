package com.simple.democdadmin.service;

import com.simple.democdadmin.vo.LoginData;
import com.simple.democdadmin.vo.ResultVo;
import com.simple.democdadmin.vo.User;

import javax.servlet.http.HttpServletRequest;

public interface UserService {
    public ResultVo captchaCode();
    public ResultVo login(LoginData loginData, HttpServletRequest request);
    public ResultVo add(User user,Long roleId);
    public ResultVo queryAll();
    public ResultVo test();
}

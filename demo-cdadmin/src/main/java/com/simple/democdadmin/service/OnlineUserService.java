package com.simple.democdadmin.service;

import com.simple.democdadmin.vo.OnlineUser;
import com.simple.democdadmin.vo.User;

import javax.servlet.http.HttpServletRequest;

public interface OnlineUserService {
    public OnlineUser save(User user, String token, HttpServletRequest request);

    public OnlineUser getOne(String token);
}

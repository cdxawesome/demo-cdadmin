package com.simple.democdadmin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 保存在线用户信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OnlineUser implements Serializable {

    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     *岗位
     */
    private String dept;

    /**
     * 浏览器信息
     */
    private String browser;

    /**
     * ip
     */
    private String ip;

    /**
     * 地址
     */
    private String address;

    /**
     * token
     */
    private String token;

    /**
     * 登陆时间
     */
    private Date loginTime;

}

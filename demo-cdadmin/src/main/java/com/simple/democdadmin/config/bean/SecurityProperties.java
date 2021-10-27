package com.simple.democdadmin.config.bean;

import lombok.Data;

@Data
public class SecurityProperties {

    /**
     * 请求头:设置为 Authorization
     */
    private String header;

    /**
     * 令牌前缀，最后留个空格 Bearer
     */
    private String tokenStartWith;

    /**
     * 令牌过期时间 这里的单位需要除以毫秒
     */
    private Long tokenValidityInSeconds;

    /**
     * 在线用户的key，用来查找redis中的token数据
     */
    private String onlineKey;

    /**
     * token续期时间范围
     */
    private Long renewal;
}

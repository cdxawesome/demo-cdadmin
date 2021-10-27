package com.simple.democdadmin.filter;

import com.simple.democdadmin.config.bean.SecurityProperties;
import com.simple.democdadmin.service.OnlineUserService;
import com.simple.democdadmin.utils.TokenProvider;
import com.simple.democdadmin.vo.OnlineUser;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class TokenFilter extends GenericFilterBean {

    private final SecurityProperties securityProperties;
    private final OnlineUserService onlineUserService;
    private final TokenProvider tokenProvider;

    /**
     * 因为filter的初始化顺序实在servlet之前的，而Spring的Bean是在servlet之后初始化的，所以
     * 这里如果直接使用Bean注入的话是无法获取到的。因此采用构造函数传参的方式
     *
     * @param securityProperties /
     * @param onlineUserService  /
     */
    public TokenFilter(SecurityProperties securityProperties, OnlineUserService onlineUserService, TokenProvider tokenProvider) {
        this.securityProperties = securityProperties;
        this.onlineUserService = onlineUserService;
        this.tokenProvider = tokenProvider;
    }

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

    /**
     * 初步处理 token
     *
     * @param request /
     * @return /
     */
    private String checkToken(HttpServletRequest request) {
        try {
            // 从请求头获取token
            String bearerToken = request.getHeader(securityProperties.getHeader());
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(securityProperties.getTokenStartWith())) {
                // 去掉令牌前缀和空格
                return bearerToken.replace(securityProperties.getTokenStartWith(), "").trim();
            } else {
                logger.debug("非法token");
            }
        } catch (Exception e) {
            logger.debug("-----------请求没有携带token-----------");
        }
        return null;
    }
}

package com.simple.democdadmin.config;

import com.simple.democdadmin.config.bean.SecurityProperties;
import com.simple.democdadmin.filter.TokenFilter;
import com.simple.democdadmin.handler.MyAccessDeniedHandler;
import com.simple.democdadmin.service.OnlineUserService;
import com.simple.democdadmin.utils.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyAccessDeniedHandler myAccessDeniedHandler;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private TokenProvider tokenProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()
                // 自定义登陆界面
                .loginPage("/login.html");
        // 放心规则
        http.authorizeRequests()
                .antMatchers("/user/login", "/user/captchaCode", "/doc.html", "/swagger/**", "/login.html").permitAll()
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/v2/**").permitAll()
                .antMatchers("/favicon.ico/**").permitAll()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/user/test").permitAll()
                .anyRequest().authenticated()
                .and()
                // 关闭csrf
                .csrf().disable();
        // 自定义登陆失败处理逻辑
        http.exceptionHandling().accessDeniedHandler(myAccessDeniedHandler);
        // 自定义前置过滤器
        http.addFilterBefore(new TokenFilter(securityProperties, onlineUserService, tokenProvider), UsernamePasswordAuthenticationFilter.class);
    }
    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}



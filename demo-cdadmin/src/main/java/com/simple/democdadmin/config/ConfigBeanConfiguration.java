package com.simple.democdadmin.config;

import com.simple.democdadmin.config.bean.LoginProperties;
import com.simple.democdadmin.config.bean.SecurityProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 从配置文件中读入配置转成pojo类
 */
@Configuration
public class ConfigBeanConfiguration {


    @Bean
    @ConfigurationProperties(prefix = "login")
    public LoginProperties loginProperties() {
        return new LoginProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "jwt")
    public SecurityProperties securityProperties() {
        return new SecurityProperties();
    }
}

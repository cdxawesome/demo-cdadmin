package com.simple.democdadmin.config.bean;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class RsaProperties {
    @Value("${rsa.private_key}")
    private String privateKey;

}

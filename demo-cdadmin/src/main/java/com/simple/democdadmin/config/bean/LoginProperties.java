package com.simple.democdadmin.config.bean;

import com.pig4cloud.captcha.*;
import com.pig4cloud.captcha.base.Captcha;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.awt.*;
import java.util.Objects;


@Data
public class LoginProperties {
    private LoginCode loginCode;

    /**
     * 获取验证码生产类
     * @return
     */
    public Captcha getCaptcha() {
        if (Objects.isNull(loginCode)) {
            loginCode = new LoginCode();
            if (Objects.isNull(loginCode.getCodeType())) {
                loginCode.setCodeType(LoginCodeEnum.arithmetic);
            }
        }
        return switchCaptcha(loginCode);
    }

    /**
     * 根据配置信息生成验证码
     */
    private Captcha switchCaptcha(LoginCode loginCode) {
        Captcha captcha = null;
        synchronized (this) {
            switch (loginCode.getCodeType()) {
                case arithmetic:
                    captcha = new ArithmeticCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    captcha.setLen(loginCode.getLength());
                    break;
                case chinese:
                    captcha = new ChineseCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    captcha.setLen(loginCode.getLength());
                    break;
                case chinese_gif:
                    captcha = new ChineseGifCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    captcha.setLen(loginCode.getLength());
                    break;
                case gif:
                    captcha = new GifCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    captcha.setLen(loginCode.getLength());
                    break;
                case spec:
                    captcha = new SpecCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    captcha.setLen(loginCode.getLength());
                    break;
            }
        }
        if (StringUtils.hasText(loginCode.getFontName())) {
            captcha.setFont(new Font(loginCode.getFontName(), Font.PLAIN, loginCode.getFontSize()));
        }
        return captcha;
    }

}

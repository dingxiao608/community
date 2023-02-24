package com.nowcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    //kaptcha有一个主要的接口，Producer，里面有两个方法，一个用来生成验证码文本，另一个就是生成验证码图片
    @Bean
    public Producer kaptchaProducer(){
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "100");
        properties.setProperty("kaptcha.image.height", "40");
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");//验证码文本从这里面选
        properties.setProperty("kaptcha.textproducer.char.length", "4");//4个文本
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");//图片没有噪声

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties); //com.google.code.kaptcha.util.Config;
        kaptcha.setConfig(config);
        return kaptcha;
    }
}

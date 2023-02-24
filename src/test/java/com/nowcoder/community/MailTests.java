package com.nowcoder.community;

import com.nowcoder.community.utils.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//指定配置类为CommunityApplication.class
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void sendTextMail(){
        mailClient.sendMail("1417359341@qq.com", "测试mail", "来自929751708的邮件");
    }

    @Test
    public void sendHtmlMail(){
        Context context = new Context();    //org.thymeleaf.context.Context
        context.setVariable("username", "tom");     //设置模板maildemo.html需要的变量

        String content = templateEngine.process("/mail/mailDemo", context); //content就是渲染后的模板内容
        System.out.println(content);

        mailClient.sendMail("1417359341@qq.com", "测试html邮件", content);
    }
}

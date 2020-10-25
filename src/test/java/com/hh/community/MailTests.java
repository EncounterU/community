package com.hh.community;

import com.hh.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest//这个注解是自带的
//以CommunityApplication作为测试类,从这个类中加载想要的数据，就是从这里进去，这个类能有的这里都能得到，还可以有一些自己写的配置什么的，
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;//模板引擎的类，里面提供了些方法
    @Test//发送普通文本邮件
    public void sendMailtest(){
        mailClient.sendMail("914935621@qq.com","第一份邮件","welcome! 黄浩");
    }
    @Test//发送html（thymeleaf模板）邮件
    public void sendMailhtmlTest(){
        Context context=new Context();//这里选择thymeleaf的Context类
        context.setVariable("username","徐玲玥");//让contex里有了username这个键值对
        String content = templateEngine.process("/mail/邮件示例", context);//连同键值对和和html一起存入content中
        System.out.println(content);
        mailClient.sendMail("914935621@qq.com","第一份邮件",content);
    }
}

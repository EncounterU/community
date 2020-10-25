package com.hh.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
//这个类帮新浪发邮件，给了授权码，算个代理
public class MailClient {
    //记录日志
    private static final Logger logger= LoggerFactory.getLogger(MailClient.class);
    //注入邮件发送的类
    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String from;
    public void sendMail(String to,String subject,String content){

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();//创建了一个邮件，但只是一个模板，空的需要自己添加东西
            MimeMessageHelper helper=new MimeMessageHelper(mimeMessage);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content,true);//不加true默认是普通文本，加了true也能识别HTML了
            mailSender.send(helper.getMimeMessage());//最后发送邮件
        } catch (MessagingException e) {
            logger.error("发送邮件失败！"+e.getMessage());//发声异常记录日志，这里用error级别的日志，e.getMessage()这是异常信息
        }
    }

}

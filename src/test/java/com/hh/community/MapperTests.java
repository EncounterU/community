package com.hh.community;
import com.hh.community.dao.DiscussPostMapper;
import com.hh.community.dao.LoginTicketMapper;
import com.hh.community.dao.UserMapper;
import com.hh.community.entity.DiscussPost;
import com.hh.community.entity.LoginTicket;
import com.hh.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
//@RunWith(),这是一个运行器,让测试在Spring容器环境下执行。如测试类中无此注解，将导致service,dao等自动注入失败。
@RunWith(SpringRunner.class)
@SpringBootTest//这个注解是自带的
//以CommunityApplication作为测试类,从这个类中加载想要的数据，就是从这里进去，这个类能有的这里都能得到，还可以有一些自己写的配置什么的，
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Test
    public void testSelectUser(){
        User user=userMapper.selectById(11);
        System.out.println(user);
        user=userMapper.selectByName("huanghao");
        System.out.println(user);
    }
    @Test
    public void testUpdateUser(){
        int rows=userMapper.updateStatus(11,0);
        System.out.println(rows);
        System.out.println(userMapper.selectById(11));
    }
    @Test
    public void testSelectDiscussPost(){
        List<DiscussPost> list=discussPostMapper.selectDiscussPosts(101,0,10);
        for (DiscussPost post : list) {
            System.out.println(post);
        }
    }
    //测试登录通行证
    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));//多久到期
        loginTicketMapper.insertLoginTicket(loginTicket);
    }
    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc", 1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }
}

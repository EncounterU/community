package com.hh.community.service;

import com.hh.community.dao.LoginTicketMapper;
import com.hh.community.dao.UserMapper;
import com.hh.community.entity.LoginTicket;
import com.hh.community.entity.User;
import com.hh.community.util.CommunityUtil;
import com.hh.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.hh.community.util.CommunityConstant.*;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;//模板引擎
    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Value("${community.path.domain}")//注入一个具体的值就用这个注解，上面的@Autowired注入的是bean
    private String domain;//接受上面这个值，这个是域名
    @Value("${server.servlet.context-path}")
    private String contextPath;//这是项目名

    public User findUserById(int id){
        return userMapper.selectById(id);
    }
    //注册，返回值有很多信息，就用个map集合来封装，传入的是User对象
    public Map<String,Object> register(User user){
        Map<String,Object> map=new HashMap<>();
        //空值处理
        if(user==null){
            throw new IllegalArgumentException("参数不能为空");//记着这种方式
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","用户名不能为空！");
            return map;//出现了这个就可以直接返回了
        }if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }
        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u !=null){
            map.put("usernameMsg","该用户名已经存在！");
            return map;
        }
        //验证邮箱
        u=userMapper.selectByEmail(user.getEmail());
        if(u !=null){
            map.put("emailMsg","该邮箱已经被注册！");
            return map;
        }
        //注册用户，也就是把user存入数据库中
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));//substring(0,5)取前面的字符串中下标0和5之间的数（前面的随机数太长了）
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));//加密后是这两部分组成的
        user.setType(0);//其他属性默认值定下来
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());//激活码随机生成
        //随机头像（牛客网），%d 是占位符，对应后面的随机数
        //format(String format, Object ... args):该方法使用指定的格式字符串和参数返回一个格式化的字符串，格式化后的新字符串使用本地默认的语言环境。
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        //注册邮件 (不懂就看下MailTests)
        Context context=new Context();
        context.setVariable("username",user.getUsername());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活邮件",content);
        return map;
    }
    //激活验证
    //激活邮件里包含userId和code，就用这两个来验证是否激活
    public int activation(int userId, String code) {
        //先得到对象，这里的逻辑是你注册成功了，数据库有数据了，但是你的status默认为0，需要激活这一步来改成1，不然你还是没法登录
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    //登录
    //这种判断问题都用map集合来装吗
    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String,Object> map=new HashMap<>();
        //空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;//出现了这种情况就结束了直接返回，下面一个道理
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }
        // 生成登录凭证，是上面都没有满足才有这一步（重点），也就是生成凭证的时候账号密码这些已经验证成功了
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));//登录状态截止日期
        loginTicketMapper.insertLoginTicket(loginTicket);//把凭证存入数据库

        map.put("ticket", loginTicket.getTicket());//把ticket这个值也存入进去可能后面要用吧
        return map;
    }
    //退出登录
    public void logOut(String ticket){
        loginTicketMapper.updateStatus(ticket,1);//1 表示无效 0表示有效
    }
    //查询凭证
    public LoginTicket findLoginTicket(String ticket){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        return loginTicket;
    }
    //更新用户头像
    public int updateHeader(int userId,String headerUrl){
        return userMapper.updateHeader(userId,headerUrl);
    }

    //更改用户密码
    public Map<String,Object> updatePassWord(int userId,String password,String newPassword,String confirmPassword){
        Map<String,Object> map=new HashMap<>();
        User user = userMapper.selectById(userId);
        String pw = user.getPassword();//原始密码
        String npassword = CommunityUtil.md5(password + user.getSalt());//加密对比
        String nnewPassword = CommunityUtil.md5(newPassword + user.getSalt());
        String nconfirmPassword = CommunityUtil.md5(confirmPassword + user.getSalt());
        if(!npassword.equals(pw)){
            map.put("passwordError","原密码不正确");
            return map;
        }
        if(nnewPassword.equals(npassword)){
            map.put("passwordSame","新密码和原密码相同");
            return map;
        }
        if(!nnewPassword.equals(nconfirmPassword)){
            map.put("passwordNotSame","两次密码不相同，重新确认");
            return map;
        }
        System.out.println(newPassword);
        userMapper.updatePassword(userId,CommunityUtil.md5(newPassword+user.getSalt()));
        return map;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }
}


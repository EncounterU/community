package com.hh.community.controller;

import com.google.code.kaptcha.Producer;
import com.hh.community.entity.User;
import com.hh.community.service.UserService;
import com.hh.community.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger= LoggerFactory.getLogger(LoginController.class);//声明日志对象，下面有用这个对象
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaProducer;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @RequestMapping(path = "/register",method = RequestMethod.GET)//get请求，下面有个post请求的
    public String getRegisterPage(){
        return "/site/register";
    }
    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }


    @RequestMapping(path = "/register",method = RequestMethod.POST)//传入数据，用post
    //model 是个模型，后面的参数往里面存，这里user对象负责接收页面上传来的值（对应属性会相匹配）
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);//
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";//成功了转到这个页面,然后这个页面就可以获得 msg target的值
        }
        else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";//没成功又返回注册页面
        }

    }
    //激活验证判断
    // http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    //@PathVariable,用来获取路径中的值的"userId"是对应路径中的名字
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    //验证码生成
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    //这里本来要有返回值的，来返回验证码，但是我们也可以直接用response和session自动接收然后返回，session的用处就来了
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码
        String text = kaptchaProducer.createText();//生成一段字符串（只含四个，是这个配置类设置好的，可以去看KaptchaConfig）
        BufferedImage image = kaptchaProducer.createImage(text);//传入字符串，组成一个图片
        //将验证码文字存入session
        session.setAttribute("kaptcha",text);
        //将图片输出给浏览器
        response.setContentType("image/png");//声明返回的是png格式的图片
        //response向浏览器响应，必须要获取输出内容的输出流,有各种输出流，字符的，字符串的，字节流的，这里用字节流适合图片？
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);//输出图片，参数分别为，图片，格式，输出流
        } catch (IOException e) {
            logger.error("响应验证码失败"+e.getMessage());
        }

    }
    //登录判断
    @RequestMapping(path = "/login", method = RequestMethod.POST)//和上面的路径一样，但是请求方法不同也可以区分开的
    //这里参数里，要和前端页面的name属性的值对应
    //下面方法的参数要是是实体类，什么Page，User这种就会直接存入Model里，但是是这种简单的String，int类型就不会自动存进去，这是一个规则
    //而这些简单的类型是存在request里的，可以从request里得到，当然这里只写了response，想也是，肯定是要在请求里，
    public String login(String username, String password, String code, boolean rememberme,
                        Model model, HttpSession session, HttpServletResponse response) {
        // 检查验证码
        String kaptcha = (String) session.getAttribute("kaptcha");
        //其中一个为空，或者不相等（不区分大小写），就失败
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";//重新返回登录页面
        }

        // 检查账号,密码
        //这里rememberme是个条件，根据 true就第一个，false第二个
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        //逻辑有点小绕，没事慢慢的，不要求一下懂完,下面这一步是把login方法的map赋值给了这个map，如果login那个方法验证成功了的话
        // ticket就也在里面
        //这里的登录截止日期和下面的cookie存活时间是一样的
        Map<String, Object> map = userService.login(username, password, expiredSeconds);//这里的登录时间和下面的cookie存活时间一样
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());//把ticket存入cookie中发给客户端
            cookie.setPath(contextPath);//生效范围，为整个项目
            cookie.setMaxAge(expiredSeconds);//生效时间
            response.addCookie(cookie);
            return "redirect:/index";//重定向到首页，重定向作用你懂吧，是路径不是资源（index.html这种算资源），forward也是路径不是资源
        } else {
            //没有登录成功就会把错误信息返回给页面
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }
    //退出登录
    @RequestMapping(path = "/logout",method = RequestMethod.GET)//就用get方法，因为这个不会提交什么数据
    public String logOut(@CookieValue("ticket") String ticket){
        userService.logOut(ticket);
        return "redirect:/login";//有两个 一个post 一个get，这里默认返回到get的请求
    }

}

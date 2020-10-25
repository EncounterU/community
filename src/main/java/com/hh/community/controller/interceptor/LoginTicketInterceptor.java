package com.hh.community.controller.interceptor;

import com.hh.community.entity.LoginTicket;
import com.hh.community.entity.User;
import com.hh.community.service.UserService;
import com.hh.community.util.CookieUtil;
import com.hh.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取凭证
        String ticket= CookieUtil.getValue(request,"ticket");
        if(ticket!=null){
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);//凭证可能有，但是过期了怎么办，所以要判断
            //通过status查看状态, loginTicket.getExpired().after(new Date()):超时时间晚于当前时间 ，都满足才有效
            if(loginTicket!=null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())){
                //根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());//这里我写成getId了，别搞错了
                //浏览器向服务器发请求，可能有很多请求，服务器都是会建立独立线程的，并发状态下可能会产生冲突，所以要线程隔离
                //在本次请求中持有用户，当前请求没有结束，这线程就一直在，那这个user就一直在当前线程的map里，当请求响应后才销毁
                hostHolder.setUser(user);//通过凭证找到用户并把用户暂存存在了这里（当前线程）
            }
        }
        return true;
    }

    @Override //模板之前调用的，就先可以把user取出来存入model，再显示到模板上
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user!=null&& modelAndView!=null){
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();//模板执行完毕，清理掉数据
    }
}

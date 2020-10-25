package com.hh.community.controller.interceptor;

import com.hh.community.annotation.LoginRequired;
import com.hh.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;//这里注入进来就是看看能不能获取到user信息（检查你登没登陆），没登录有些路径不能直接访问

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断拦截的是方法不
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;//如果是方法，先强转下
            Method method = handlerMethod.getMethod();//再获取方法
            //从这个方法上去取注解（LoginRequired.class），有的方法可能没加这个注解，所以又能为空
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            //这里进行下判断，假如不为空（就代表你加了这个注解），又没有登录（没查询到user），你就不准去访问，只能回到登录页面
            if (loginRequired != null && hostHolder.getUser() == null) {
                //这里用不了return 的方式重定向（是boolean型），当前方法有response，就利用这个重定向吧；request.getContextPath() 还可以这样获得路径
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;//这里要去WebConfig里注册，使得除了静态资源都拦截，但只是对加了注解的进行重定向处理
    }
}

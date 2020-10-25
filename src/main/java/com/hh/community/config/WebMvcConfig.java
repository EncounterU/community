package com.hh.community.config;

import com.hh.community.controller.interceptor.AlphaInterceptor;
import com.hh.community.controller.interceptor.LoginRequiredInterceptor;
import com.hh.community.controller.interceptor.LoginTicketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
//设置mvc的配置，当然拦截器可以在这里设置，就是controller的时候生效
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private AlphaInterceptor alphaInterceptor;//这只是个例子

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor)//注册一个拦截器，只写这一句代表拦截所有的路径了
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg")//exclude 是排除的意思
                .addPathPatterns("/register","/login");//添加拦截路径
        // /** :表示static下的所有文件夹 ，/*表示static下的某一文件夹 ， .css表示以.css结尾的的文件,总的就是包含了所有以.css结尾的文件

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");//除了静态资源都拦截了

        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        //这个对应的是用加注解的方式来拦截，比一个个的写路径方便点
    }
}

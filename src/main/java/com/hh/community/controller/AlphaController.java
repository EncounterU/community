package com.hh.community.controller;

import com.hh.community.service.AlphaService;
import com.hh.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//这里是一些小demo的例子
@Controller
@RequestMapping("/alpha")//请求路径(这是简化版，默认get请求，其他请求也可以)
public class AlphaController {
    @Autowired
     private AlphaService alphaService;
    @RequestMapping("/1")
    @ResponseBody //这是表明返回的是字符型，不加就会默认返回的是html
    public String method(){
        return "hello springboot!";
    }
    @RequestMapping("/2")
    @ResponseBody
    public String method1(){
        return alphaService.find();
    }

    //GET请求有以下两种传参方式;get请求是向服务器获取数据
    // 路径:/students  当前页：current  每页多少数据：limit  格式：students?current=1&limit=10
    @RequestMapping(path = "/students",method = RequestMethod.GET)//强制方法为get方法
    @ResponseBody
    public String getStudents(@RequestParam(name = "current",required = false,defaultValue = "1") int current,
                              @RequestParam(name = "limit",required = false,defaultValue = "20") int limit){
        System.out.println(current);
        System.out.println(limit);
        return "somestudent";
    }
    // 路径:/student/123
    @RequestMapping(path = "/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id) {
        System.out.println(id);
        return "a student";
    }

    //Post请求，向服务器提交数据
    @RequestMapping(path = "/student",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return "success";
    }
    // 响应HTML数据 以下两种方式
    @RequestMapping(path = "/teacher",method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView mav=new ModelAndView();
        mav.addObject("name","张三");
        mav.addObject("age",22);
        mav.setViewName("/demo/view");
        return mav;
    }
    @RequestMapping(path = "school",method = RequestMethod.GET)
    public String getSchool(Model model){
        model.addAttribute("name","北大");
        model.addAttribute("age",100);
        return "/demo/view";
    }
    //相应json数据（异步请求）
    // 响应JSON数据(异步请求)
    // Java对象 -> JSON字符串 -> JS对象（浏览器是js响应）
    @RequestMapping(path = "/emp",method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String,Object>> getEmp(){
        List<Map<String,Object>> list=new ArrayList<>();
        Map<String,Object> emp=new HashMap<>();
        emp.put("name","黄浩");
        emp.put("age",22);
        emp.put("salary",10000);
        list.add(emp);

        emp=new HashMap<>();
        emp.put("name","黄");
        emp.put("age",21);
        emp.put("salary",10000);
        list.add(emp);
        return list;

    }
    //cookie
    @RequestMapping(path = "/cookie/set" ,method = RequestMethod.GET)
    @ResponseBody
    //HttpServletResponse response,也可以只写这个
    public String setCookie(HttpServletRequest request, HttpServletResponse response){
        //创建cookie对象
        Cookie cookie=new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie生效范围
        cookie.setPath("/community/alpha");///community/alpha这个路径下的都可以
        //设置cookie生效时间
        cookie.setMaxAge(60*10);//60s * 10
        //发送cookie（响应回去）
        response.addCookie(cookie);
        return "set cookie 。。。点点";
    }
    @RequestMapping(path = "/cookie/get1" ,method = RequestMethod.GET)
    @ResponseBody
    //public String getCookie(@CookieValue("code") String code){},这种直接取叫“code”的cookie，并传给参数code
    public String getCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            String value = cookie.getValue();
            System.out.println(name+":"+value);
        }
        return "get cookie";
    }
    // session示例

    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    //和cookie不同，session不需要手动创建，和request，response，model这些对象一样，只需要声明为参数就可以注入进来了
    public String setSession(HttpSession session) {
        session.setAttribute("id", 1);
        session.setAttribute("name", "Test");
        return "set session";
    }

    @RequestMapping(path = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session) {
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }

    //AJAX 示例
    @RequestMapping(path = "/ajax",method = RequestMethod.POST)
    @ResponseBody//因为是异步请求所以不向浏览器返回网页，而是返回字符串,用这个注解
    public  String testAjax(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0,"操作成功！");
    }

}

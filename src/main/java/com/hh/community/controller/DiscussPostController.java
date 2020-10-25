package com.hh.community.controller;

import com.hh.community.entity.DiscussPost;
import com.hh.community.entity.User;
import com.hh.community.service.DiscussPostService;
import com.hh.community.service.UserService;
import com.hh.community.util.CommunityUtil;
import com.hh.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

//帖子相关操作
@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;//用户信息居然都用这个来获取，值得注意

    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody //这里返回的是字符串，不是网页，所以用这个注解
    public String addDiscussPost(String title,String content){
        User user = hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403,"你还没有登录！");
        }
        DiscussPost post=new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());//其他都有默认值
        discussPostService.addDiscussPost(post);
        //报错的情况将来统一处理
        return CommunityUtil.getJSONString(0,"发布成功！");
    }
    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)//需要返回模板（是个路径，就用String吧）
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model){
        //帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);//model专门存这种对象
        //作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
        return "/site/discuss-detail";
    }
}

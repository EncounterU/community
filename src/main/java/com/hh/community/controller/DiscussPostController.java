package com.hh.community.controller;

import com.hh.community.entity.Comment;
import com.hh.community.entity.DiscussPost;
import com.hh.community.entity.Page;
import com.hh.community.entity.User;
import com.hh.community.service.CommentService;
import com.hh.community.service.DiscussPostService;
import com.hh.community.service.LikeService;
import com.hh.community.service.UserService;
import com.hh.community.util.CommunityConstant;
import com.hh.community.util.CommunityUtil;
import com.hh.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

//帖子相关操作
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;//用户信息居然都用这个来获取，值得注意

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

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
    //帖子详情展示（默认正序）
    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)//需要返回模板（是个路径，就用String吧）
    //Page,来接收分页条件，因为这是一个实体类型（不是基本的类型，string ，int这种），会自动存入model中
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);//model专门存这种对象
        //作者（这是帖子作者）
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
        //点赞数量
        long likeCount =likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeCount",likeCount);
        //点赞状态(万一没登录肯定是未赞)
        int likeStatus=hostHolder.getUser()==null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeStatus",likeStatus);

        //评论的分页信息
        page.setLimit(5);//每一页显示五条数据
        page.setPath("/discuss/detail/"+discussPostId);//这一句有点不懂
        page.setRows(post.getCommentCount());//帖子表里有这个字段可以直接看有多少评论数

        //命名规范   评论：给帖子的评论；回复:给评论的评论
        //评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //评论展示列表
        List<Map<String,Object>> commentVoList=new ArrayList<>();
        if(commentList!=null){
            for(Comment comment:commentList){
                //建立一个Map集合来装评论信息
                Map<String,Object> commentMap=new HashMap<>();
                //评论
                commentMap.put("comment",comment);
                //作者 （评论作者）
                commentMap.put("user",userService.findUserById(comment.getUserId()));
                //点赞数量
                likeCount =likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentMap.put("likeCount",likeCount);
                //点赞状态(万一没登录肯定是未赞)
                likeStatus=hostHolder.getUser()==null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentMap.put("likeStatus",likeStatus);

                //展示回复列表  从第一条数据开始，不分页，把数据显示完
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String,Object>> replyVoList=new ArrayList<>();
                if(replyList!=null){
                    for(Comment reply:replyList){
                        Map<String,Object> replyMap=new HashMap<>();
                        //回复
                        replyMap.put("reply",reply);
                        replyMap.put("user",userService.findUserById(reply.getUserId()));
                        //回复目标 等于0代表是评论的楼主，而不是评论里的评论,这里是获取这个被回复的用户（或者没有）
                        User target=reply.getTargetId()==0 ? null:userService.findUserById(reply.getTargetId());
                        replyMap.put("target",target);
                        //点赞数量
                        likeCount =likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyMap.put("likeCount",likeCount);
                        //点赞状态(万一没登录肯定是未赞)
                        likeStatus=hostHolder.getUser()==null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyMap.put("likeStatus",likeStatus);
                        replyVoList.add(replyMap);
                    }
                }
                //这里就是建立一个map类型的List集合，然后创建map集合去存键值对，然后再存入list集合，（list集合也可以往map里存）
                commentMap.put("replys",replyVoList);
                //回复数量,没有展示评论数量
                int replyCount=commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentMap.put("replyCount",replyCount);
                //再把map装进list里
                commentVoList.add(commentMap);
            }
        }
        //最后把最终结果存入model里
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }
}

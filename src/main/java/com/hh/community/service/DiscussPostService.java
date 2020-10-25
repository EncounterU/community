package com.hh.community.service;

import com.hh.community.dao.DiscussPostMapper;
import com.hh.community.entity.DiscussPost;
import com.hh.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;
    public List<DiscussPost> getDiscussPosts(int userId,int offset,int limit){
        return discussPostMapper.selectDiscussPosts(userId,offset,limit);
    }
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }
    //添加帖子
    public int addDiscussPost(DiscussPost post){
        if(post==null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        //转义HTML标记 让文本不识别HTML的标记功能？当成正常的文本
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        //插入数据
        return discussPostMapper.insertDiscussPost(post);
    }
    //首页点击查询帖子
    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }
}

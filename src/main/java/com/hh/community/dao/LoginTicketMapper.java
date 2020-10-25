package com.hh.community.dao;

import com.hh.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {
    //这里没用对应的mapper.xml来写sql，而是直接在这里的写的，两种方式都行
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })//这里允许分开写好看点，会自动帮你拼接的 注意逗号和空格
    @Options(useGeneratedKeys = true, keyProperty = "id")//声明 id自动生成这个，你懂吧 记住一下这个注解
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\"> ",
            "and 1=1 ",
            "</if>",
            "</script>"
    })//动态sql，需要加这个<script>来表示
    int updateStatus(String ticket, int status);//更改凭证状态，生成了一般不删，留着可以以后统计什么的，只需要该状态让它失效即可

}

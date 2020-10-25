package com.hh.community.controller;

import com.hh.community.annotation.LoginRequired;
import com.hh.community.entity.User;
import com.hh.community.service.UserService;
import com.hh.community.util.CommunityUtil;
import com.hh.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    private static Logger logger= LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;//要知道当前用户是谁，就把这调进来？
    //设置页面
    @LoginRequired//自定义注解
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }
    //上传
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    //mvc专门提供的MultipartFile类来接收上传的文件，这里只要上传一个文件（一张图片），多个就用数组 MultipartFile[] ； headerImage参数名和前端的name属性对应
    public String uploadheader(MultipartFile headerImage, Model model){
        if(headerImage==null){
            model.addAttribute("error","你还没有选择图片");
            return "/site/setting";
        }
        //判断后缀合不合理
        String fileName = headerImage.getOriginalFilename();//得到这文件的原始文件名
        //substring():返回字符串的子字符串。 lastIndexOf 返回的是一个int数据，表示从 "."后开始的索引 。最后返回的比如是 png
        String suffix = fileName.substring(fileName.lastIndexOf("."));//后缀(有可能是空的，一般是 png)
        //检验是否为空
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }
        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放路径
        File dest=new File(uploadPath+"/"+fileName);
        //把图片存进来,下面这个是一个好方法，记住
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败"+e.getMessage());//记录下异常信息
            throw  new RuntimeException("上传文件失败，服务器发生异常",e);
        }
        //更新当前用户的头像的路径（web访问路径，不是本地的了）
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;//对应上面那一行路径,就是找到下面这个路径吧
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)//这里路径
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放路径（假装服务器和自己的电脑不是同一台）
        fileName=uploadPath+ "/" +fileName;
        //文件后缀（一样的 操作）
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片(etContentType() 这个方法了解下)
        response.setContentType("image/" + suffix);
        //图片是二进制，要用到字节流
        try ( OutputStream os = response.getOutputStream();//得到输出流（需要一个输入流来读取这个输出流，下面）
              FileInputStream fis=new FileInputStream(fileName);//读取fileName这个文件获取一个输入流，有了这个输入流之后就可以开始输出了
              //输入流是我们自己创建的，springmvc不会帮我们用完后关闭（输出流可以，写这里和下面无所谓），所以写到这里，后面会自动放入finally方法里关闭（前提要有close()方法）
        ){
            //OutputStream os = response.getOutputStream();输出流写这里也可以，response管理的属于mvc
            byte[] buffer=new byte[1024];//需要一个缓冲区（buffer），一批一批的输出（1024个字节）
            int b=0;//这个方法流那里讲了的，去复习一遍
            while ((b=fis.read(buffer)) != -1){
                os.write(buffer,0,b);//输出
            }

        } catch (IOException e) {
            logger.error("读取头像失败："+ e.getMessage());
        }
    }

    //修改密码
    @RequestMapping(path = "/updatePassword" ,method = RequestMethod.POST)
    public String updatePassword( Model model,String password,String newPassword,String confirmPassword){
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassWord(user.getId(), password,newPassword,confirmPassword);
        //这里map为空和null要一起排除，我也不深究了，null和空不一样，如果改密码成功，map很明显为空的，下面这个if就不满足
        if(map!=null && !map.isEmpty()){
            model.addAttribute("passwordError",map.get("passwordError"));
            model.addAttribute("passwordSame",map.get("passwordSame"));
            model.addAttribute("passwordNotSame",map.get("passwordNotSame"));
            return "/site/setting";
        }
        else {
            return "redirect:/index";
        }
    }
}

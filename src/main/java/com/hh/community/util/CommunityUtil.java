package com.hh.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        //java 自带的UUID类，生成随机字符串，并且吧所有的"-"替换掉（为空）
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密
    // hello -> abc123def456 只能加密，不能解密，同样的密码加密是固定的，相同的
    // hello + 3e4a8 -> abc123def456abc  密码加一个随机字符串，再加密，破解难度更大了，更安全
    public static String md5(String key) {
        //为空就不加密了，不处理;StringUtils就是那个依赖里的类
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());// 默认参数是byte类型，所以key.getBytes(),把key从string类型转为了byte类型再加密，
    }

    //获取json格式的数据
    public static String getJSONString(int code, String msg, Map<String,Object> map){
        //创建一个json对象
        JSONObject json=new JSONObject();
        //把信息装进去
        json.put("code",code);
        json.put("msg",msg);
        if(map!=null){
            //遍历map放入json
            for(String key:map.keySet()){
                json.put(key,map.get(key));
            }
        }
        return json.toJSONString();//最后再把这个json对象转换成json格式的字符串 （也就是key value吧）
    }
    //方法重写 code参数必须要有
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "黄浩");
        map.put("age", 22);
        System.out.println(getJSONString(0, "ok", map));//{"msg":"ok","code":0,"name":"黄浩","age":22}
    }

}

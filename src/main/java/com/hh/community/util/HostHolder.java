package com.hh.community.util;

import com.hh.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息,用于代替session对象.session是线程隔离的，但是这里不想用session？所以用这个来线程隔离user
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();//底层是获取了当前线程，根据当前对象获取一个map，然后把值存在map里

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }//请求结束的时候就要把当前的user清理掉，remove()也是获取当前线程清理

}

package com.hh.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//元注解描述，声明下面的注解(LoginRequired)是准备用在方法上
@Retention(RetentionPolicy.RUNTIME)//声明该注解有效时机（这里是运行时才有效）
public @interface LoginRequired {
}

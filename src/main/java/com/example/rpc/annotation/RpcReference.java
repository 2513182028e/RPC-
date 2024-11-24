package com.example.rpc.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})//用于属性上
@Retention(RetentionPolicy.RUNTIME)//运行时
public @interface RpcReference {
    public  String referenceName();

}

package com.example.rpc.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})  //用于类上
@Retention(RetentionPolicy.RUNTIME) //用于运行时
public @interface RpcService {  //服务注入，根据name 注入，将该注释的类加载到Beans中

    public String serviceName();
}

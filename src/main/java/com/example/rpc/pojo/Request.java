package com.example.rpc.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Request {

    private int requestId;

    private String className;

    private  String methodName;

    private Class<?>[] paramTypes;

    private Object[] parameters;

    private String serviceNameByAnnotation;// 通过注解名字获取服务名称




}

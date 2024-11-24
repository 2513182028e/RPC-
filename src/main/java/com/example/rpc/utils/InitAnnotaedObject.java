package com.example.rpc.utils;


import com.example.rpc.annotation.RpcService;
import org.apache.ibatis.javassist.tools.reflect.Reflection;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InitAnnotaedObject {



    public static Map<String,Object> init()
    {
            Map<String,Object> map=new ConcurrentHashMap<>();
            try {

                Reflections reflection = new Reflections("com.example.rpc");
                Set<Class<?>> annotated = reflection.getTypesAnnotatedWith(RpcService.class);
                annotated.stream().forEach(clazz->{
                    if(clazz.isAnnotationPresent(RpcService.class))
                    {
                        //RpcService rpcService = clazz.getAnnotation(RpcService.class);
                        try {
                            Object o = clazz.newInstance();
                            map.put(clazz.getName(),o);
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }

                    });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return map;
    }


}

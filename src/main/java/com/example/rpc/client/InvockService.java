package com.example.rpc.client;


import com.example.rpc.annotation.RpcReference;
import com.example.rpc.constants.RpcConstants;
import com.example.rpc.pojo.Book;
import com.example.rpc.pojo.MessageContext;
import com.example.rpc.pojo.Request;
import com.example.rpc.server.pService.BookService;
import com.example.rpc.server.pService.BookServiceImpl;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

public class InvockService {



    @RpcReference(referenceName = "BookServiceImpl")
    private BookService bookService;

    Class<?> clazz;
    NettyClientHandler nettyClientHandler;

    private final ExecutorService executorService = Executors.newFixedThreadPool(8);//创建一个线程池


    public InvockService(Class<?> clazz,NettyClientHandler nettyClientHandler)
    {
            this.clazz=clazz;
            this.nettyClientHandler=nettyClientHandler;
    }

    public Object getBean()
    {

        //通过proxy代理对象创建被调用者的代理对象
        Object proxyInstance = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz.getInterfaces()[0]}, (proxy, method, args) -> {
            //重写调用逻辑，但对象被代理后调用代理对象的方法时，会自动进入这个方法逻辑里面去
            Request request = new Request();
            request.setClassName(clazz.getName());
            request.setMethodName(method.getName());
            request.setParamTypes(method.getParameterTypes());
            request.setParameters(args);
            //request.setRequestId(); request的ID暂时不设置，留在encode阶段去设置即可
            Field field = InvockService.class.getDeclaredField("bookService");

            field.setAccessible(true);
            //request.setServiceNameByAnnotation(field.getAnnotation(RpcReference.class).referenceName());
            MessageContext messageContext = MessageContext.builder()
                    .messageType(RpcConstants.REQUEST_TYPE)
                    .compress((byte) 1)
                    .codec((byte) 1)
                    .data(request)
                            .build();
            nettyClientHandler.setMessageContext(messageContext);
            //Object o = nettyClientHandler.called();
            Object o = executorService.submit(nettyClientHandler).get();
            return o;

        });
        return  proxyInstance;
    }
//public static void main(String[] args) throws NoSuchFieldException {
//    Book book = new Book(1, "2");
//    Class<? extends BookServiceImpl> bookImplClass = new BookServiceImpl().getClass();
//    Class<? extends Book> aClass = book.getClass();
//    System.out.println(book.getClass().getDeclaredField("name"));
//    System.out.println(Arrays.toString(bookImplClass.getInterfaces()));
//}


}

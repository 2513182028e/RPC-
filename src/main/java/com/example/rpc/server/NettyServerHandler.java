package com.example.rpc.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.rpc.constants.RpcConstants;
import com.example.rpc.pojo.MessageContext;
import com.example.rpc.pojo.Request;
import com.example.rpc.pojo.Response;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.java.Log;
import org.springframework.cglib.reflect.FastClass;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {


    private Map<String,Object> beans;

    public Map<String,Object> getBeans() {
        return beans;
    }

    public void setObjectMap(Map<String, Object> objectMap) {
            this.beans=objectMap;
    }
    public NettyServerHandler(){
        super();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("服务端 NioServerSocketChannel 已经和主eventLoopGroup中的某一个eventLoop绑定注册完成 Address ====== " + ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到来自客户端的消息 ====== " + msg);
        if (msg instanceof MessageContext){
            channelRead0(ctx,(MessageContext) msg);
        }

    }

    //事件触发
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
       if (evt instanceof IdleStateEvent)
       {
           IdleStateEvent stateEvent=(IdleStateEvent) evt;
           if(stateEvent.state()==IdleState.ALL_IDLE)
           {
               System.out.println("已经60秒没有收到读写消息了，关闭连接");
               ctx.close();
           }
       }else{
           super.userEventTriggered(ctx, evt);
       }
    }

    protected void channelRead0(ChannelHandlerContext ctx, MessageContext o) throws Exception {
        Object result=null;
        Object context = o.getData();
        byte messageType = o.getMessageType();
        int LastId = o.getRequestId();
        if(messageType==RpcConstants.HEARTBEAT_REQUEST_TYPE)
        {
            //心跳包则不予回复即可
            return;
        }
        Request request = JSON.parseObject(JSON.toJSONString(context), Request.class);

        Class<?>[] paramTypes = request.getParamTypes();
        Object[] parameters = request.getParameters();

        System.out.println("服务端中context为： "+"type"+"----为"+paramTypes);


        if(paramTypes.length==parameters.length)
        {
            for (int i=0;i<parameters.length;i++)
            {
                Object od=JSON.parseObject(JSONObject.toJSONString(parameters[i]),paramTypes[i]);
                parameters[i]=od;
            }
        }
        //JSON对与类的存储是不同的，Class<?> Class Integer  ，而字符串为“java.ing.Integer”
        System.out.println("Request为: "+request);
        Class<?> aClass = Class.forName(request.getClassName());

        if (beans == null || beans.size() == 0) {//我这里逻辑没写，可以自己加(request.getClassName()传类的全限定名就可以)
            System.out.println("没有找到被调用的bean实例");
            ctx.writeAndFlush(new MessageContext(LastId, RpcConstants.FAIL_TYPE, (byte) 1, (byte) 1,new Response(LastId,false,new Object(),Object.class)));
            return;
        }

        Object o1 = beans.get(request.getClassName());

        if(o1==null)
        {
            System.out.println("beans里没找到实例，根据Class创建");
             o1 = aClass.newInstance();

        }


        Method method = aClass.getMethod(request.getMethodName(), request.getParamTypes());
        method.setAccessible(true);
        Class<?> returnType = method.getReturnType();
        Object invoked = method.invoke(o1, parameters);
        if(returnType.isInstance(invoked))
        {
            result=returnType.cast(invoked);
        }
        System.out.println("调用完成，返回结果："+result.toString());
        ctx.writeAndFlush(new MessageContext(LastId, RpcConstants.RESPONSE_TYPE, (byte) 1, (byte) 1,new Response(LastId,true,result,returnType)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("发生了异常");
        cause.printStackTrace();
        ctx.close();
    }

}

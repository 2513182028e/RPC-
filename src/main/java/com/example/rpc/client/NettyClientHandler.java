package com.example.rpc.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.rpc.constants.RpcConstants;
import com.example.rpc.pojo.MessageContext;
import com.example.rpc.pojo.Request;
import com.example.rpc.pojo.Response;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.Callable;

public class NettyClientHandler extends ChannelInboundHandlerAdapter implements  Callable {


    private ChannelHandlerContext ctx;


    private MessageContext messageContext;

    //private Promise<MessageContext> promise=new DefaultPromise<>(new DefaultEventLoop());

    public NettyClientHandler(){
        super();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       System.out.println("客户端连接建立了");
       this.ctx=ctx;

    }

    @Override
    public  void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("来自服务端的消息："+msg);
        if(msg instanceof MessageContext)
        {
            System.out.println("调用read0方法");
            channelRead0(ctx,(MessageContext)msg);

        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("已经超过30秒没有和RPC服务器进行读写操作了，将发送心跳信息");
        if (evt instanceof IdleStateEvent)
        {
            IdleStateEvent stateEvent=(IdleStateEvent)evt;
            if(stateEvent.state()== IdleState.ALL_IDLE)
            {
                MessageContext context = MessageContext.builder()
                        .codec((byte) 1)
                        .compress((byte) 1)
                        .messageType(RpcConstants.HEARTBEAT_REQUEST_TYPE)
                        .data(new Object()).build();
                ctx.writeAndFlush(context);
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }

    public  synchronized void channelRead0(ChannelHandlerContext ctx, MessageContext msg){

        System.out.println("调用服务端后的返回值为:"+msg.toString());
        //promise.setSuccess(msg);
        System.out.println("调用服务端后时此刻的线程为:"+Thread.currentThread().getName());
        this.messageContext=msg;
        notify();
        //promise.setSuccess(msg);
        System.out.println("---");
            //notify();  //唤醒下面阻塞等待调用结果的call线程


    }


    @Override
    public synchronized Object call() throws Exception {
     System.out.println("called时此刻的线程为:"+Thread.currentThread().getName());
    System.out.println("调用了call方法");
    System.out.println("messageContext的值为： "+messageContext);
        ChannelFuture future = ctx.writeAndFlush(messageContext);
        //ChannelFuture future = ctx.writeAndFlush("123");
        future.addListener(new ChannelFutureListener(){

            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(channelFuture.isSuccess())
                {
                    System.out.println("Complete时此刻的线程为:"+Thread.currentThread().getName());
                    System.out.println("发送消息成功");
                }else{
                    System.out.println("发送消息失败");
                }
            }
        });
        //Thread.sleep(4000);
        //MessageContext context = promise.get();

        wait();//发送完消息等待结果返回;
        System.out.println("wait后时此刻的线程为:"+Thread.currentThread().getName());
        MessageContext context=this.messageContext;
        System.out.println("得到了相应结果"+context);
    //当线程运行到下面的时候，意味着this.messageContext已经被赋值了，也就是服务端返回了结果


        Response response = JSON.parseObject(JSONObject.toJSONString(context.getData()), Response.class);
        //Response response = new Response(true, new Object(), Object.class);
        System.out.println("response:"+response);
        return JSON.parseObject(JSONObject.toJSONString(response.getContext()), response.getContextType());


        //Response response = new Response(true, new Object(), Object.class);
    }

//    @Override
//    public synchronized Object call() throws Exception {
//    System.out.println("调用了call方法");
//    System.out.println("messageContext的值为： "+messageContext);
//    //ctx.writeAndFlush("123");
//    ctx.writeAndFlush(messageContext);
//
//    Thread.sleep(4000);
//    //wait();//发送完消息等待结果返回;
//    System.out.println("得到的结果为："+this.messageContext);
//
//    //当线程运行到下面的时候，意味着this.messageContext已经被赋值了，也就是服务端返回了结果
//        //Response response = JSON.parseObject(JSONObject.toJSONString(this.messageContext.getContext()), Response.class);
//        Response response = new Response(true, new Object(), Object.class);
//        return JSON.parseObject(JSONObject.toJSONString(response.getContext()), response.getContextType());
//
//    }

    public void setMessageContext(MessageContext messageContext) {
        this.messageContext = messageContext;
    }
    public MessageContext getMessageContext() {
        return  messageContext;
    }
}

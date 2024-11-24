package com.example.rpc.client;

import com.example.rpc.Nacos.NacosService;
import com.example.rpc.Nacos.NacosServiceImpl;
import com.example.rpc.common.MessageDecoder;
import com.example.rpc.common.MessageDecoder1;
import com.example.rpc.common.MessageEncoder;
import com.example.rpc.common.MessageEncoder1;
import com.example.rpc.pojo.Book;
import com.example.rpc.pojo.MessageContext;
import com.example.rpc.pojo.ServiceMeta;
import com.example.rpc.server.pService.BookService;
import com.example.rpc.server.pService.BookServiceImpl;
import com.example.rpc.utils.CustomLoggingHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NettyClient {



    public static void main(String[] args) throws InterruptedException {

        NacosServiceImpl nacosService = new NacosServiceImpl();
        NioEventLoopGroup group = new NioEventLoopGroup();
        NettyClientHandler nettyClientHandler = new NettyClientHandler();
        Bootstrap bootstrap = new Bootstrap();
        System.out.println("client主线程:"+Thread.currentThread().getName());
        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new CustomLoggingHandler(LogLevel.TRACE));
                            pipeline.addLast(new MessageDecoder1());
                            pipeline.addLast(new MessageEncoder1());
                            pipeline.addLast(new IdleStateHandler(0,0,7));
                            pipeline.addLast(nettyClientHandler);
                        }
                    });
            List<ServiceMeta> discovery = nacosService.discovery("netty");
            ServiceMeta serviceMeta = discovery.get(0);
            ChannelFuture channelFuture = bootstrap.connect(serviceMeta.getIP(), serviceMeta.getPort()).sync();

            if (channelFuture.isSuccess()) {
                System.out.println("连接服务器成功，开始调用服务");

               BookService proxyInstance = (BookService)new InvockService(BookServiceImpl.class, nettyClientHandler).getBean();
                Book book = proxyInstance.getBook(0);
                System.out.println("执行了getBook后的结果:"+book);
//                proxyInstance.updateBook(book);
//                System.out.println("执行了updateBook后的结果:"+book);
            }
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(group!=null)
            {
                group.shutdownGracefully();
            }

        }

    }



};

package com.example.rpc.server;

import com.example.rpc.Nacos.NacosServiceImpl;
import com.example.rpc.common.MessageDecoder;
import com.example.rpc.common.MessageDecoder1;
import com.example.rpc.common.MessageEncoder;
import com.example.rpc.common.MessageEncoder1;
import com.example.rpc.pojo.Book;
import com.example.rpc.pojo.ServiceMeta;
import com.example.rpc.utils.CustomLoggingHandler;
import com.example.rpc.utils.InitAnnotaedObject;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.ArrayList;
import java.util.List;

public class NettyServer {

    static List<Book> lists=new ArrayList<>();
    private static final String IP="127.0.0.1";
    private static final int PORT=8090;
    private static final String SERVICE_NAME="netty";



    public static void main(String[] args) throws InterruptedException {
        NacosServiceImpl nacosService = new NacosServiceImpl();
        lists.add(new Book(1,"java"));
        lists.add(new Book(2,"python"));
        NettyServerHandler nettyServerHandler = new NettyServerHandler();
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
    try {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss,worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("logging",new CustomLoggingHandler(LogLevel.TRACE));
                        pipeline.addLast(new MessageDecoder1());
                        pipeline.addLast(new MessageEncoder1());
                        //心跳检测，服务器在60s内从channel中没有读到数据，或者没有写入过数据，则触发一个事件
                        pipeline.addLast(new IdleStateHandler(0,0,60));
                        pipeline.addLast(nettyServerHandler);
                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(8090).sync();
        if(channelFuture.isSuccess())
        {
            nacosService.register(new ServiceMeta(SERVICE_NAME,IP,PORT));
            nettyServerHandler.setObjectMap(InitAnnotaedObject.init());
            System.out.println("成功加载bean,数量："+nettyServerHandler.getBeans().size());
            System.out.println("已加载的beans：--["+nettyServerHandler.getBeans().toString()+"]--");
            System.out.println("服务器已启动。。。");

        }
        channelFuture.channel().closeFuture().sync();
    }catch (Exception e)
    {
        e.printStackTrace();
    }finally {
        boss.shutdownGracefully();
        worker.shutdownGracefully();
    }
    }
}

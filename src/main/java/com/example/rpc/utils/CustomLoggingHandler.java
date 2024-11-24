package com.example.rpc.utils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class CustomLoggingHandler extends LoggingHandler {


    public CustomLoggingHandler(LogLevel logLevel)
    {
        super(logLevel);
    }

    @Override
    protected String format(ChannelHandlerContext ctx, String eventName, Object args) {
        String name = Thread.currentThread().getName();
        return String.format("[%s] %s: %s",name,eventName,args);
    }
}

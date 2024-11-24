package com.example.rpc.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.rpc.pojo.MessageContext;
import com.example.rpc.serialize.ProtustuffSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    private final  static ProtustuffSerializer protustuffSerializer = new ProtustuffSerializer();
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String s = new String(bytes, StandardCharsets.UTF_8);

         System.out.println("decoder出来的字符串为:"+s);
        MessageContext messageContext = protustuffSerializer.deserializer(bytes, MessageContext.class);
        //MessageContext messageContext = JSON.parseObject(s, MessageContext.class);
        list.add(messageContext);
        byteBuf.skipBytes(byteBuf.readableBytes());
    }
}

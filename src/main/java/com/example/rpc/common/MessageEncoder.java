package com.example.rpc.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.rpc.pojo.MessageContext;
import com.example.rpc.serialize.ProtustuffSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessageEncoder  extends MessageToByteEncoder {

    private final  static ProtustuffSerializer protustuffSerializer = new ProtustuffSerializer();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        System.out.println("进入了encoder: "+o);
        byte[] bytes = protustuffSerializer.serialize(o);
        //String s = JSON.toJSONString(o);
        byteBuf.writeBytes(bytes);
        System.out.println("已经写入了到通道了：");
    }
}

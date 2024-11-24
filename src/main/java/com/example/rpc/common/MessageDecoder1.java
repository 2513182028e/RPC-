package com.example.rpc.common;

import com.example.rpc.compress.GzipCompress;
import com.example.rpc.constants.RpcConstants;
import com.example.rpc.pojo.MessageContext;
import com.example.rpc.pojo.Request;
import com.example.rpc.pojo.Response;
import com.example.rpc.serialize.ProtustuffSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.protostuff.Rpc;

import java.util.Arrays;
import java.util.List;

public class MessageDecoder1 extends LengthFieldBasedFrameDecoder {

     private static  final ProtustuffSerializer protustuffSerializer =new ProtustuffSerializer();
    private static  final GzipCompress gzipCompress =new GzipCompress();

    public MessageDecoder1() {
        super(RpcConstants.MAX_FRAME_LENGTH, 5,4,-9 ,0);
    }
    public MessageDecoder1(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(RpcConstants.MAX_FRAME_LENGTH, 5,4,-9 ,0);
    }

    public MessageDecoder1(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }


    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if(decoded instanceof ByteBuf)
        {
            ByteBuf frame=(ByteBuf)decoded;
            if(frame.readableBytes()>RpcConstants.TOTAL_LENGTH)
            {
                try {
                    return  decodeFrame(frame);
                }catch (Exception e)
                {
                    throw new RuntimeException(e);
                }finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }


    public Object decodeFrame(ByteBuf in)
    {
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength=in.readInt();
        //建立 一个消息对象
        byte messageType = in.readByte();
        byte codec = in.readByte();
        byte compress= in.readByte();
        int requestId=in.readInt();

        //读取真实数据
        MessageContext messageContext = MessageContext.builder()
                .messageType(messageType)
                .codec(codec)
                .compress(compress)
                .requestId(requestId).build();
        //判断是否为心跳包
        if(messageType==RpcConstants.HEARTBEAT_REQUEST_TYPE)
        {
            messageContext.setData(RpcConstants.PING);
        }
        if(messageType==RpcConstants.HEARTBEAT_RESPONSE_TYPE)
        {
            messageContext.setData(RpcConstants.PONG);
        }
        //如果不是心跳包
        int bodyLength=fullLength- RpcConstants.HEAD_LENGTH;
        if(bodyLength>0)
        {
            //读取到bytes中
            byte[] bytes = new byte[bodyLength];
            in.readBytes(bytes);
            //解压缩
            bytes= gzipCompress.decompress(bytes);
            //deserializer
            if(messageType==RpcConstants.REQUEST_TYPE)
            {
                Request request = protustuffSerializer.deserializer(bytes, Request.class);
                messageContext.setData(request);
            }
            if(messageType==RpcConstants.RESPONSE_TYPE)
            {
                Response response = protustuffSerializer.deserializer(bytes, Response.class);
                messageContext.setData(response);
            }
        }
        return messageContext;
    }

     private void checkVersion(ByteBuf in) {
        // read the version and compare
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        // read the first 4 bit, which is the magic number, and compare
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }
}

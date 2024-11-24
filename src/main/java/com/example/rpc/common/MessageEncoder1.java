package com.example.rpc.common;

import com.example.rpc.compress.GzipCompress;
import com.example.rpc.constants.RpcConstants;
import com.example.rpc.pojo.MessageContext;
import com.example.rpc.pojo.Request;
import com.example.rpc.serialize.ProtustuffSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * custom protocol decoder
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 *
 *
 */
public class MessageEncoder1 extends MessageToByteEncoder<MessageContext> {


    private static  final AtomicInteger ATOMIC_INTEGER=new AtomicInteger(0);
    private static  final ProtustuffSerializer protustuffSerializer =new ProtustuffSerializer();
    private static  final GzipCompress gzipCompress =new GzipCompress();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext,  MessageContext o, ByteBuf out) throws Exception {
        try {
            byte[] compress=null;
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(RpcConstants.VERSION);
            //leave 4 bytes for data length
            out.writerIndex(out.writerIndex()+4);

            //消息类型
            byte messagetype = o.getMessageType();
            out.writeByte(messagetype);
            //codec 1B
            out.writeByte(o.getCodec());//codec这里改为1B的占位符
            //compress 1B
            out.writeByte(o.getCompress());
            //requestId;


//            }
            int fullLength=RpcConstants.HEAD_LENGTH;//前面的全部字节数为16B
            if(messagetype==RpcConstants.HEARTBEAT_REQUEST_TYPE)  //如果是心跳包请求
            {
                int Id=ATOMIC_INTEGER.getAndIncrement();
                out.writeInt(Id);
                o.setRequestId(Id);
                byte[] bytes = RpcConstants.PING.getBytes();
                compress = gzipCompress.compress(bytes);
                fullLength+=compress.length;
            }

            //body(内容）
            if(messagetype!=RpcConstants.HEARTBEAT_REQUEST_TYPE&&messagetype!=RpcConstants.HEARTBEAT_RESPONSE_TYPE)
            {
                if(messagetype==RpcConstants.REQUEST_TYPE)
                {
                     int Id=ATOMIC_INTEGER.getAndIncrement();
                      out.writeInt(Id);
                      o.setRequestId(Id);
                      Request request = (Request) o.getData();
                      request.setRequestId(Id);
                }
                else{
                    out.writeInt(o.getRequestId());
                }
                //如果不是心跳检测包
                byte[] bytes = protustuffSerializer.serialize(o.getData());
                 compress = gzipCompress.compress(bytes);
                fullLength+=compress.length;//全部长度（B）
            }

            if (compress.length>0)
            {
                out.writeBytes(compress);//写入真实数据
            }
            int writeIndex=out.writerIndex(); //获取当前的指针的坐标
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            out.writeInt(fullLength);
            out.writerIndex(writeIndex);  //调整写指针的位置

        }catch (Exception e)
        {
            System.out.println("Encode request error!");
        }

    }


}

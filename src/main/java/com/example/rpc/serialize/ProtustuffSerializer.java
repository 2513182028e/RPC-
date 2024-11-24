package com.example.rpc.serialize;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
// protostuff序列化，其优点在于数据量小，速度快
public class ProtustuffSerializer implements Serializer{

    private static final LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);


    @Override
    public byte[] serialize(Object object) {
        Schema schema = RuntimeSchema.getSchema(object.getClass());
        byte[] bytes;
        try {
            bytes=ProtobufIOUtil.toByteArray(object,schema,BUFFER);
        }finally {
            BUFFER.clear();
        }
        return bytes;
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T t = schema.newMessage();
        ProtobufIOUtil.mergeFrom(bytes,t,schema);
        return  t;
    }
}

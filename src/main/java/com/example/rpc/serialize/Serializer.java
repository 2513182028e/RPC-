package com.example.rpc.serialize;

public interface Serializer {




    byte[] serialize(Object object);


    <T> T deserializer(byte[] bytes,Class<T> clazz);


}

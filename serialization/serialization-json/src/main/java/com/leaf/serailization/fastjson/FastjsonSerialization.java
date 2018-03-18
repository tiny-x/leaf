package com.leaf.serailization.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.api.SerializerType;

public class FastjsonSerialization implements Serializer {

    @Override
    public SerializerType serializerType() {
        return SerializerType.FAST_JSON;
    }

    @Override
    public <T> byte[] serialize(T object) {
        byte[] bytes = JSON.toJSONBytes(object, SerializerFeature.WriteNullNumberAsZero);
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        T t = JSON.parseObject(data, clazz);
        return t;
    }
}

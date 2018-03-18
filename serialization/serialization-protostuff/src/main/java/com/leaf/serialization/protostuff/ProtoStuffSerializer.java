package com.leaf.serialization.protostuff;

import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.api.SerializerType;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtoStuffSerializer implements Serializer {

    private static final ThreadLocal<LinkedBuffer> bufThreadLocal = new ThreadLocal<LinkedBuffer>() {
        @Override
        protected LinkedBuffer initialValue() {
            return LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        }
    };

    @Override
    public SerializerType serializerType() {
        return SerializerType.PROTO_STUFF;
    }

    /**
     * 序列化（对象 -> 字节数组）
     *
     * @param <T>    the type parameter
     * @param object the object
     * @return the byte [ ]
     */
    @SuppressWarnings("unchecked")
    public <T> byte[] serialize(T object) {
        LinkedBuffer buffer = bufThreadLocal.get();
        try {
            Schema<T> schema = RuntimeSchema.getSchema((Class<T>) object.getClass());
            return ProtostuffIOUtil.toByteArray(object, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化（字节数组 -> 对象）
     *
     * @param <T>  the type parameter
     * @param data the data
     * @return the t
     */
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        try {
            Schema<T> schema = RuntimeSchema.getSchema(clazz);
            T message = schema.newMessage();
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}

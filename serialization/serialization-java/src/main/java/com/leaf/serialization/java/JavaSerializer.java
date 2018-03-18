package com.leaf.serialization.java;

import com.leaf.common.utils.AnyThrow;
import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.api.SerializerType;

import java.io.*;

public class JavaSerializer implements Serializer {

    private static final ThreadLocal<ByteArrayOutputStream> threadLocal = new ThreadLocal<ByteArrayOutputStream>() {
        @Override
        protected ByteArrayOutputStream initialValue() {
            return new ByteArrayOutputStream();
        }
    };

    @Override
    public SerializerType serializerType() {
        return SerializerType.JAVA;
    }

    @Override
    public <T> byte[] serialize(T object) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (ObjectOutput out = new ObjectOutputStream(buf)) {
            out.writeObject(object);
            out.flush();
            return buf.toByteArray();
        } catch (Exception e) {
            AnyThrow.throwUnchecked(e);
        } finally {
            buf.reset();
        }
        return null;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        try (
                ByteArrayInputStream buf = new ByteArrayInputStream(data);
                ObjectInput input = new ObjectInputStream(buf);
        ) {
            T t = (T) input.readObject();
            return t;
        } catch (Exception e) {
            AnyThrow.throwUnchecked(e);
        }
        return null;
    }
}

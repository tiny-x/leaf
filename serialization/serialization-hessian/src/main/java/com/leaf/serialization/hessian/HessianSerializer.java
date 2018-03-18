package com.leaf.serialization.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.leaf.common.utils.AnyThrow;
import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.api.SerializerType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerializer implements Serializer {

    private static final ThreadLocal<ByteArrayOutputStream> threadLocal = new ThreadLocal<ByteArrayOutputStream>() {
        @Override
        protected ByteArrayOutputStream initialValue() {
            return new ByteArrayOutputStream();
        }
    };

    @Override
    public SerializerType serializerType() {
        return SerializerType.HESSIAN;
    }

    @Override
    public <T> byte[] serialize(T object) {
        ByteArrayOutputStream buf = threadLocal.get();
        Hessian2Output output = new Hessian2Output(buf);
        try {
            output.writeObject(object);
            output.flush();
            return buf.toByteArray();
        } catch (IOException e) {
            AnyThrow.throwUnchecked(e);
        } finally {
            buf.reset();
            try {
                output.close();
            } catch (IOException e) {
            }
        }
        return null;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        Hessian2Input input = null;
        try (ByteArrayInputStream buf = new ByteArrayInputStream(data)) {
            input = new Hessian2Input(buf);
            T t = (T) input.readObject(clazz);
            return t;
        } catch (IOException e) {
            AnyThrow.throwUnchecked(e);
            try {
                if (input != null)
                    input.close();
            } catch (IOException e1) {
            }
        }
        return null;
    }
}

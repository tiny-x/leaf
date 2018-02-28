package com.leaf.serialization.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public class SerializerFactory {

    private static ServiceLoader<Serializer> serializers;

    private static Map<SerializerType, Serializer> serializerMap = new HashMap<>();

    static {
        serializers = ServiceLoader.load(Serializer.class);
        Iterator<Serializer> iterator = serializers.iterator();
        while (iterator.hasNext()) {
            Serializer serializer = iterator.next();
            serializerMap.put(serializer.serializerType(), serializer);
        }
    }

    public static Serializer serializer(SerializerType serializerType) {
        Serializer serializer = serializerMap.get(serializerType);
        if (serializer == null) {
            throw new IllegalArgumentException("Illegal serializerType : " + serializerType);
        } else {
            return serializer;
        }
    }
}

package com.leaf.common.utils;

import com.esotericsoftware.reflectasm.MethodAccess;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author yefei
 * @date 2018-02-26 11:12
 */
public class Reflects {

    private static final ConcurrentHashMap<Class<?>, SoftReference<MethodAccess>> METHODACCESS_CACHE = new ConcurrentHashMap<>();

    public static Object Invoke(Object obj, String methodName, Object[] args) {
        Class<?> aClass = obj.getClass();
        SoftReference<MethodAccess> methodAccess = METHODACCESS_CACHE.get(aClass);

        if (methodAccess == null) {
            SoftReference<MethodAccess> newMethodAccess = new SoftReference<>(MethodAccess.get(aClass));
            methodAccess = METHODACCESS_CACHE.putIfAbsent(aClass, newMethodAccess);
            if (methodAccess == null) {
                methodAccess = newMethodAccess;
            }
        }
        MethodAccess invoker = methodAccess.get();
        if (invoker == null) {
            METHODACCESS_CACHE.remove(aClass);
            invoker = MethodAccess.get(aClass);
        }
        return invoker.invoke(obj, methodName, args);
    }

    public static Object getTypeDefaultValue(Class<?> clazz) {
        checkNotNull(clazz, "clazz");

        if (clazz.isPrimitive()) {
            if (clazz == byte.class) {
                return (byte) 0;
            }
            if (clazz == short.class) {
                return (short) 0;
            }
            if (clazz == int.class) {
                return 0;
            }
            if (clazz == long.class) {
                return 0L;
            }
            if (clazz == float.class) {
                return 0F;
            }
            if (clazz == double.class) {
                return 0D;
            }
            if (clazz == char.class) {
                return (char) 0;
            }
            if (clazz == boolean.class) {
                return false;
            }
        }
        return null;
    }
}

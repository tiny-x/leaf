package com.leaf.common.utils;

import com.esotericsoftware.reflectasm.MethodAccess;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

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
}

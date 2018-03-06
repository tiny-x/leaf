package com.leaf.common.utils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public enum Proxies {

    JDK_PROXY(new ProxyDelegate() {

        @Override
        public <T> T newProxy(Class<T> interfaceType, Object handler) {

            Object object = Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class<?>[]{interfaceType},
                    (InvocationHandler) handler);

            return interfaceType.cast(object);
        }
    }), //

    BYTE_BUDDY(new ProxyDelegate() {

        @Override
        public <T> T newProxy(Class<T> interfaceType, Object handler) {
            Class<? extends T> cls = new ByteBuddy().subclass(interfaceType)
                    .method(ElementMatchers.isDeclaredBy(interfaceType))
                    .intercept(MethodDelegation.to(handler, "handler")).make()
                    .load(interfaceType.getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();
            Object obj = null;
            try {
                obj = cls.newInstance();
            } catch (Exception e) {
                AnyThrow.throwUnchecked(e);
            }
            return interfaceType.cast(obj);
        }
    });

    private final ProxyDelegate delegate;

    Proxies(ProxyDelegate delegate) {
        this.delegate = delegate;
    }

    public static Proxies getDefault() {
        return BYTE_BUDDY;
    }

    public <T> T newProxy(Class<T> interfaceType, Object handler) {
        return delegate.newProxy(interfaceType, handler);
    }

    interface ProxyDelegate {

        <T> T newProxy(Class<T> interfaceType, Object handler);
    }
}

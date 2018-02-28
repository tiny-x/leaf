/*
 * Copyright (c) 2015 The Jupiter Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.leaf.common.utils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author yefei
 * @date 2017 -6-26 10:51:41 The enum Proxies.
 */
public enum Proxies {
    /**
     * The Jdk proxy.
     */
    JDK_PROXY(new ProxyDelegate() {

        @Override
        public <T> T newProxy(Class<T> interfaceType, Object handler) {

            Object object = Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class<?>[]{interfaceType},
                    (InvocationHandler) handler);

            return interfaceType.cast(object);
        }
    }), //

    /**
     * The Byte buddy.
     */
    BYTE_BUDDY(new ProxyDelegate() {

        @Override
        public <T> T newProxy(Class<T> interfaceType, Object handler) {
            Class<? extends T> cls = new ByteBuddy().subclass(interfaceType)
                    .method(ElementMatchers.isDeclaredBy(interfaceType))
                    .intercept(MethodDelegation.to(handler, "handler")).make()
                    .load(interfaceType.getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();
            Object obj;
            try {
                obj = cls.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return interfaceType.cast(obj);
        }
    });

    private final ProxyDelegate delegate;

    Proxies(ProxyDelegate delegate) {
        this.delegate = delegate;
    }

    /**
     * Gets default.
     *
     * @return the default
     */
    public static Proxies getDefault() {
        return BYTE_BUDDY;
    }

    /**
     * New proxy t.
     *
     * @param <T>           the type parameter
     * @param interfaceType the interface type
     * @param handler       the handler
     * @return the t
     */
    public <T> T newProxy(Class<T> interfaceType, Object handler) {
        return delegate.newProxy(interfaceType, handler);
    }

    interface ProxyDelegate {

        /**
         * Returns a proxy instance that implements {@code interfaceType} by
         * dispatching method invocations to {@code handler}. The class loader
         * of {@code interfaceType} will be used to define the proxy class.
         *
         * @param <T>           the type parameter
         * @param interfaceType the interface type
         * @param handler       the handler
         * @return the t
         */
        <T> T newProxy(Class<T> interfaceType, Object handler);
    }
}

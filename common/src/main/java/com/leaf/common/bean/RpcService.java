package com.leaf.common.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Rpc service.
 *
 * @author yefei
 * @date 2017 -06-20 14:09
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {

    /**
     * Value class.
     *
     * @return the class
     */
    Class<?> value();

    /**
     * Version string.
     *
     * @return the string
     */
    String version() default "1.0.0";
}

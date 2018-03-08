package com.leaf.common.annotation;

import com.leaf.common.constants.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceProvider {

    /**
     * 服务版本
     *
     * @return
     */
    String version() default Constants.DEFAULT_SERVICE_VERSION;

    /**
     * 权重
     *
     * @return
     */
    int weight() default Constants.SERVICE_WEIGHT;

}

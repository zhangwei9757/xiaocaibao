package com.tumei.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by leon on 2016/12/19.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LimitAnnotation {
    /**
     * 允许访问的次数
     * @return
     */
    int count() default Integer.MAX_VALUE;

    /**
     * 时间段
     * @return
     */
    long time() default 60000;

    /**
     * 1: 最低的角色级别
     * 2: ..
     * 3: ....
     *
     * @return
     */
    int level() default 1;
}

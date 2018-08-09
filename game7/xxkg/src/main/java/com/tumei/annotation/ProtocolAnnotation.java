package com.tumei.annotation;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2017/3/3 0003.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProtocolAnnotation {
	String Name() default "";
}

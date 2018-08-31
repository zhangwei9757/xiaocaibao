package com.tumei;

import com.tumei.annotation.LimitAnnotation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Created by leon on 2016/12/19.
 */
//@Aspect
//@Component
public class SecurityAspect {
    private Log log = LogFactory.getLog(SecurityAspect.class);

    // 指定安全访问控制的切入点
    @Pointcut("execution(public * com.tumei.controller..*.*(..))")
    public void checkSecurity() {}

    @Before("checkSecurity()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        log.info("doBefore:..:" + joinPoint.toLongString());
        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        if (ms != null) {
            LimitAnnotation annotation = ms.getMethod().getAnnotation(LimitAnnotation.class);
            if (annotation != null) {
                if (annotation.level() < 3) {
                    log.error("授权级别不足访问此方法.");
                    throw new Exception("授权级别不足访问方法");
                }
            }
        }
    }
}

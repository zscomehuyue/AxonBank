package com.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Service;

@Aspect
@Service
public class LogAop {

    @Pointcut("execution(public * org.axonframework..*.*(..)))")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint pj) {
        MethodSignature methodSignature = (MethodSignature) pj.getSignature();
        String name = pj.getTarget().getClass().getSimpleName();
        System.out.println("=" + name + "." + methodSignature.getMethod().getName() + "=>");
        try {
            return pj.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }


}

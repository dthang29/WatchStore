package com.pet_project.watchstore.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(public * *(..)) && (" +
            "@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Service))")
    public Object logControllerAndService(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();
        long start = System.nanoTime();
        log.info("Bắt đầu {}", method);

        try {
            Object result = joinPoint.proceed();
            log.info("Hoàn thành {} trong {} ms", method, elapsedMillis(start));
            return result;
        } catch (Throwable ex) {
            log.error("Lỗi {} sau {} ms: {}", method, elapsedMillis(start), ex.getClass().getSimpleName());
            throw ex;
        }
    }

    private static long elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}

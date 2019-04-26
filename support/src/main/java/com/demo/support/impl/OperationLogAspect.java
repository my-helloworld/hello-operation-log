package com.demo.support.impl;

import com.demo.support.annotation.Operation;
import com.demo.support.model.OperationLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

/**
 * 通用日志切片, 支持使用 {@link Operation} 修饰函数进行日志上报
 */
@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogStack stack;

    private final OperationReporter logService;

    public OperationLogAspect(
        OperationLogStack stack,
        OperationReporter logService
    ) {
        this.stack = stack;
        this.logService = logService;
    }

    /**
     * 日志记录调用切片
     *
     * @param pjp 调用上下文
     * @return 调用结果
     * @throws Throwable 调用异常
     */
    @Around("@annotation(com.demo.support.annotation.Operation)")
    public Object recordOperation(@Nonnull ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Operation annotation = method.getAnnotation(Operation.class);
        stack.push(annotation);

        boolean success = true;
        try {
            return pjp.proceed(); // origin logic

        } catch (Throwable err) {
            success = false;      // mark failure
            throw err;

        } finally {
            OperationLog log = stack.pop();
            if (log != null) {
                log.setSuccess(success);
                //
                // Do async upload
                //
                logService.append(log);
            }
        }
    }
}

package com.demo.support.impl;

import com.demo.support.annotation.Operation;
import com.demo.support.model.OperationLog;
import com.demo.support.utils.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 每个线程包含一个日志栈, 注入的日志对象为栈顶元素
 */
@Order
@Component
public class OperationLogStack implements FactoryBean<OperationLog> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationLogStack.class);

    /**
     * 时间格式
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 操作日志线程调用栈
     */
    private final ThreadLocal<List<OperationLog>> threadStack = new InheritableThreadLocal<>();


    /**
     * 添加一条日志上下文
     *
     * @param annotation 函数修饰注解
     */
    public void push(Operation annotation) {
        List<OperationLog> stack = threadStack.get();
        if (stack == null) {
            stack = new ArrayList<>();
            threadStack.set(stack);
        }

        OperationLog logRef = createLog(annotation);
        stack.add(logRef);
    }

    /**
     * 推出日志栈
     *
     * @return 栈顶元素
     */
    @Nullable
    public OperationLog pop() {
        List<OperationLog> stack = null;
        try {
            stack = threadStack.get();
            if (stack == null || stack.isEmpty()) {
                LOGGER.warn("Try to pop an empty stack!");
                return null;
            }
            return stack.remove(0);
        } finally {
            if (stack == null || stack.isEmpty()) {
                // Do recycle while stack is empty
                threadStack.remove();
            }
        }
    }

    @Override
    public OperationLog getObject() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(OperationLog.class);
        enhancer.setCallback(new ThreadLogInvocationHandler());
        return (OperationLog) enhancer.create();
    }

    @Override
    public Class<OperationLog> getObjectType() {
        return OperationLog.class;
    }

    /**
     * 构造一条操作日志
     *
     * @param annotation 函数修饰注解
     * @return 操作日志对象
     */
    @Nonnull
    private OperationLog createLog(Operation annotation) {
        OperationLog logRef = new OperationLog();
        logRef.setLevel(annotation.level());
        Collections.addAll(logRef.getTags(), annotation.tags());
        logRef.setTimestamp(getTimestamp());
        logRef.setLocalIp(IpUtils.getLocalInet4Address());
        return logRef;
    }

    /**
     * 获取时间戳信息
     *
     * @return 时间戳格式字符串
     */
    @Nonnull
    private String getTimestamp() {
        //
        // Sick! According to AquariusUtil by aquarius service!!
        //
        return dateFormat.format(new Date()).replaceFirst(" ", "T");
    }

    /**
     * 使用调用栈顶元素作为代理对象
     */
    private class ThreadLogInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(
            Object proxy,
            Method method,
            Object[] args
        ) throws Throwable {
            List<OperationLog> stack = threadStack.get();
            OperationLog logHead = (stack == null || stack.isEmpty())
                ? new OperationLog() // Create a dummy log reference to avoid exception
                : threadStack.get().get(0);
            return method.invoke(logHead, args);
        }
    }

}

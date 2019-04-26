package com.demo.support.annotation;

import com.demo.support.model.OperationLevel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志调用注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface Operation {

    /**
     * 操作日志级别
     *
     * @return 操作日志级别
     */
    OperationLevel level() default OperationLevel.INFO;

    /**
     * 操作类型
     *
     * @return 操作类型
     */
    String type();

    /**
     * 操作描述
     *
     * @return 操作描述
     */
    String desc();

    /**
     * 操作tags
     *
     * @return 操作tags
     */
    String[] tags() default {};

}

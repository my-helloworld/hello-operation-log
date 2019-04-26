package com.demo.support.impl;

import com.demo.support.model.OperationLog;

import javax.annotation.Nonnull;

/**
 * 用户行为日志
 */
public interface OperationReporter {

    /**
     * 在独立事务上下文中添加CloudOS业务日志
     * 如果失败将不会影响当前执行事务上下文
     *
     * @param log 业务日志
     */
    void append(@Nonnull OperationLog log);

}

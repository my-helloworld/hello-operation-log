package com.demo.support.model;

/**
 * 操作日志记录级别
 */
public enum OperationLevel {

    TRACE("common.operationLog.trace"),

    INFO("common.operationLog.info"),

    DEBUG("common.operationLog.debug"),

    WARN("common.operationLog.warn"),

    ERROR("common.operationLog.error");

    private String value;

    OperationLevel(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}

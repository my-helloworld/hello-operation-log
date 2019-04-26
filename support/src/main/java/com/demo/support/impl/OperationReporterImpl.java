package com.demo.support.impl;

import com.demo.support.model.OperationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;


/**
 * 日志上报服务实现
 */
@Service
public class OperationReporterImpl implements OperationReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationReporterImpl.class);

    @Override
    public void append(@Nonnull OperationLog log) {
        // 这里只是个演示，实际可以使任何形式的上报
        LOGGER.info("{}", log);
    }

}

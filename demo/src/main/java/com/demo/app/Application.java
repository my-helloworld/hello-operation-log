package com.demo.app;

import com.demo.support.annotation.Operation;
import com.demo.support.model.OperationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chpengzh.
 * @since 2019-04-26
 */
@SpringBootApplication
public class Application {

    private static final AtomicInteger COUNT = new AtomicInteger();

    @Component
    public static class TestRunner {

        private static final Logger LOGGER = LoggerFactory.getLogger(TestRunner.class);

        @Autowired
        private OperationLog log;

        @Operation(
            type = "runner-type",
            tags = {"demo", "run"},
            desc = "测试用例"
        )
        public void run() {
            int businessId = COUNT.getAndIncrement();
            try {
                LOGGER.info("业务{}.1", businessId);
                log.getAnnotations().add("==>1");

                LOGGER.info("业务{}.2", businessId);
                log.getAnnotations().add("==>2");

                if (Objects.equals("a", new String("a".getBytes()))) {
                    throw new RuntimeException("业务" + businessId + ".啊我死了!");
                }

            } catch (RuntimeException err) {
                LOGGER.info("业务" + businessId + ".啊我死了!");
                log.getAnnotations().add(err.getMessage());
                throw err;

            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        ExecutorService executor = Executors.newFixedThreadPool(3);
        TestRunner runner = context.getBean(TestRunner.class);
        for (int i = 0; i < 5; i++) {
            executor.submit(runner::run);
        }

        Thread.sleep(1000);
        context.close();
        executor.shutdownNow();
    }

}

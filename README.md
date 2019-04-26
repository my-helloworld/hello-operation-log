# 业务日志切面

之前一个小朋友(目测年纪比我大)跑来询问了一个问题:

项目里面经常有各种各样的业务日志。这些日志通常情况下为一个文档，即带多个键值对属性，且有一个成功失败的标志。

至于日志的上报模式， 有的和业务在同一事务上下文，有的是独立于业务事务上下文，有的是异步日志记录(如ELK收集)。

> 如果以Java为样例代码，形如:

```
boolean isSuccess;
ReportContext context;
try {
    Object result = businessProcess(); // 原始业务过程
    //
    // 在上下文中写如业务成功参数
    //
    context = doSomethingWithResult(result); 
    return result;
    
} catch (Throwable err) {
    //
    // 在上下文中写入业务失败参数
    //
    context = doSomethingWithError(err);     
    throw err;
    
} finally {
    //
    // 不管成功还是失败总是执行上报
    //
    reporter.report(context);                
}
```

其中`reporter`是一个抽象上报器，具体实现由具体业务方来制定。

这样的代码如果总是以这种格式，由业务方自己去编写，则业务线代码可读性会下降一个档次(事实上我同事写的很多代码都是这个尿性)。而要解决这个问题，针对这些不同的日志收集方式，我会选择封装一个统一的SDK供业务方使用。


## 从线程调用栈出发

线程日志栈的解决方案主要思想参考了Spring的`Thread Scope`模式的Bean实现。

我们的业务代码时常是跑在业务线程池中。在每一个工作线程中，我们假设有一个日志栈。

- 当进入到待记录的函数时, 我们创建一个`ReportContext`并压入线程栈

- 当业务执行过程中, 业务函数总是操作栈顶元素`ReportContext`(添加事件，修改属性等)

- 当退出带记录的函数是，我们总是取出栈顶元素并执行上报(`reporter::report`)

样例代码参考如下:

```
public class DemoService {
    
    private final ThreadLocal<List<OperationLog>> localRef = new InheritableThreadLocal<>();
    
    private final OperationLog log = getAutowiredDelegate(); // Thread scope autowired delegate 
    
    public void doSomething() {
        //
        // Be designed as aspect
        // ↓ ↓ ↓ ↓
        push();
        // ↑ ↑ ↑ ↑
        
        try {
            // Business function here
            log.set("someRuntimeKey", "someRuntimeValue");
            
        } finally{
            
            // ↓ ↓ ↓ ↓
            OperationLog top = pop();
            reportAsync(log);
            // ↑ ↑ ↑ ↑
            // Be designed as aspect
            //
        }
    }
        
    private OperationLog getAutowiredDelegate() {
        //
        // A proxy delegate by thread local reference
        //
    }
}
```

## 一个实现样例

日志切面的实现参考`OperationLogAspect`


Demo中定义了一个被日志监管的业务函数

```java
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
```

在线程池中运行，其结果如下

```
2019-04-26 22:39:16 [pool-1-thread-3] 业务0.1
2019-04-26 22:39:16 [pool-1-thread-2] 业务1.1
2019-04-26 22:39:16 [pool-1-thread-1] 业务2.1
2019-04-26 22:39:16 [pool-1-thread-2] 业务1.2
2019-04-26 22:39:16 [pool-1-thread-3] 业务0.2
2019-04-26 22:39:16 [pool-1-thread-1] 业务2.2
2019-04-26 22:39:16 [pool-1-thread-3] 业务0.啊我死了!
2019-04-26 22:39:16 [pool-1-thread-1] 业务2.啊我死了!
2019-04-26 22:39:16 [pool-1-thread-2] 业务1.啊我死了!
2019-04-26 22:39:16 [pool-1-thread-2] OperationLog{uuid='a279a6ac-7e36-4a5e-863d-266bba53b57b', level=common.operationLog.info, tags=[demo, run], annotations=[==>1, ==>2, 业务1.啊我死了!], timestamp='2019-04-26T22:39:16', localIp='192.168.53.58', success=false}
2019-04-26 22:39:16 [pool-1-thread-1] OperationLog{uuid='6c5e689f-a4af-40c9-8873-53d504c782dc', level=common.operationLog.info, tags=[demo, run], annotations=[==>1, ==>2, 业务2.啊我死了!], timestamp='2019-04-26T22:39:16', localIp='192.168.53.58', success=false}
2019-04-26 22:39:16 [pool-1-thread-3] OperationLog{uuid='5d9c6552-fe37-44c6-b95c-64664d0af2a5', level=common.operationLog.info, tags=[demo, run], annotations=[==>1, ==>2, 业务0.啊我死了!], timestamp='2019-04-26T22:39:16', localIp='192.168.53.58', success=false}
2019-04-26 22:39:16 [pool-1-thread-3] 业务3.1
2019-04-26 22:39:16 [pool-1-thread-1] 业务4.1
2019-04-26 22:39:16 [pool-1-thread-3] 业务3.2
2019-04-26 22:39:16 [pool-1-thread-1] 业务4.2
2019-04-26 22:39:16 [pool-1-thread-3] 业务3.啊我死了!
2019-04-26 22:39:16 [pool-1-thread-1] 业务4.啊我死了!
2019-04-26 22:39:16 [pool-1-thread-3] OperationLog{uuid='03890d8b-efa3-4913-ac54-97c6f97c6cb9', level=common.operationLog.info, tags=[demo, run], annotations=[==>1, ==>2, 业务3.啊我死了!], timestamp='2019-04-26T22:39:16', localIp='192.168.53.58', success=false}
2019-04-26 22:39:16 [pool-1-thread-1] OperationLog{uuid='652df431-c9ea-4a2a-b4f8-26c2689fa96b', level=common.operationLog.info, tags=[demo, run], annotations=[==>1, ==>2, 业务4.啊我死了!], timestamp='2019-04-26T22:39:16', localIp='192.168.53.58', success=false}
```


package com.atguigu.gmall.cart.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class AsyncConfig  implements AsyncConfigurer {
    //配置线程池，控制线程数
    @Override
    public Executor getAsyncExecutor() {

        return null;
    }

    //配置统一异常处理类
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;

    }
}

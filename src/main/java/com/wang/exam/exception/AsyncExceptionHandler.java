package com.wang.exam.exception;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
//异步任务的异常处理器
public class AsyncExceptionHandler implements AsyncConfigurer {

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable ex, Method method, Object... params) -> {
            System.err.println("=====> 异步任务异常：方法=" + method.getName()
                    + "，参数=" + java.util.Arrays.toString(params)
                    + "，异常=" + ex.getMessage());
            ex.printStackTrace();
        };
    }
}
package com.wang.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
//定义一个专门给判卷任务用的线程池
public class AsyncConfig {

    //@Async("scoreExecutor")会按名字找线程池
    @Bean("scoreExecutor")
    public Executor scoreExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数
        executor.setCorePoolSize(4);
        //最大线程数
        executor.setMaxPoolSize(8);
        //最大等待线程数
        executor.setQueueCapacity(200);
        //线程统一开头命名
        executor.setThreadNamePrefix("score-");

        //队列满、线程满时，任务不再进入线程池，而是由提交者线程（AnswerService.submit的那个Tomcat线程）自己执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
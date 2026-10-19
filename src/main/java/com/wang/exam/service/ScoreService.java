package com.wang.exam.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
//异步判卷的调度入口
public class ScoreService {

    private final ScoreRemoteService scoreRemoteService;

    public ScoreService(ScoreRemoteService scoreRemoteService) {
        this.scoreRemoteService = scoreRemoteService;
    }

    //把方法调用丢到scoreExecutor线程池，异步执行
    @Async("scoreExecutor")
    public void scoreAsync(Long recordId) {
        System.out.println("==> 进入异步判卷，recordId=" + recordId
                + "，线程=" + Thread.currentThread().getName());
        scoreRemoteService.doScore(recordId);
    }
}
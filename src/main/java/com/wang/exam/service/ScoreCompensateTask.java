package com.wang.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wang.exam.entity.AnswerRecord;
import com.wang.exam.mapper.AnswerRecordMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
//定时补偿任务
public class ScoreCompensateTask {

    //最大重试次数
    private static final int MAX_RETRY = 3;
    //上次判卷服务的启动情况
    private Boolean lastHealthy = null;

    private final AnswerRecordMapper answerRecordMapper;
    private final ScoreRemoteService scoreRemoteService;
    private final RestClient scoreRestClient;

    public ScoreCompensateTask(AnswerRecordMapper answerRecordMapper,
                               ScoreRemoteService scoreRemoteService,
                               RestClient scoreRestClient) {
        this.answerRecordMapper = answerRecordMapper;
        this.scoreRemoteService = scoreRemoteService;
        this.scoreRestClient = scoreRestClient;
    }

    //启动时检查一次判卷服务
    @EventListener(ApplicationReadyEvent.class)     //spring boot完全启动后
    public void onReady() {
        boolean healthy = checkScoreServiceHealth();
        if (healthy) {
            System.err.println("判卷服务已启动\n");
        } else {
            System.err.println("【警告】判卷服务未启动\n");
        }
        lastHealthy = healthy;
    }

    @Scheduled(fixedDelay = 30_000)     //每30秒执行一次
    public void scheduled() {
        //检测判卷服务状态变化
        boolean healthy = checkScoreServiceHealth();
        if (lastHealthy != null && lastHealthy != healthy) {
            if (healthy) {
                System.err.println("判卷服务开启\n");
            } else {
                System.err.println("【警告】判卷服务关闭\n");
            }
        }
        lastHealthy = healthy;

        if (healthy) {
            recoverFailed();    //判卷服务健康时，重置FAILED记录
        }
        compensate();           //补偿判卷记录
    }

    //重置FAILED记录
    private void recoverFailed() {
        QueryWrapper<AnswerRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "FAILED");
        List<AnswerRecord> failedList = answerRecordMapper.selectList(wrapper);

        if (failedList.isEmpty()) return;

        System.out.println(">>> 重置 " + failedList.size() + " 条 FAILED 记录为 PENDING_SCORE");

        for (AnswerRecord record : failedList) {
            record.setStatus("PENDING_SCORE");
            record.setRetryCount(0);
            answerRecordMapper.updateById(record);
        }
    }

    //补偿判卷记录
    private void compensate() {
        QueryWrapper<AnswerRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PENDING_SCORE")
                .lt("retry_count", MAX_RETRY)
                .last("LIMIT 10");
        List<AnswerRecord> pendingList = answerRecordMapper.selectList(wrapper);

        if (pendingList.isEmpty()) return;

        int success = 0;
        int failed = 0;

        for (AnswerRecord record : pendingList) {
            boolean ok = scoreRemoteService.doScore(record.getId());
            if (ok) success++;
            else failed++;
        }

        System.out.println(">>> 补偿任务：处理 " + pendingList.size() + " 条"
                + "，成功 " + success + " 条，失败 " + failed + " 条\n");
    }

    //检查判卷服务的启动情况
    private boolean checkScoreServiceHealth() {
        try {
            scoreRestClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
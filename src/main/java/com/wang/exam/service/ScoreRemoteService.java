package com.wang.exam.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wang.exam.dto.ScoreRequest;
import com.wang.exam.dto.ScoreResponse;
import com.wang.exam.entity.AnswerRecord;
import com.wang.exam.entity.Question;
import com.wang.exam.mapper.AnswerRecordMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
//判分数，并处理判卷失败
public class ScoreRemoteService {

    private static final int MAX_RETRY = 3;
    private static final String REASON = "判卷服务未启动";

    private final AnswerRecordMapper answerRecordMapper;
    private final RestClient scoreRestClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final QuestionService questionService;

    public ScoreRemoteService(AnswerRecordMapper answerRecordMapper,
                              RestClient scoreRestClient,
                              QuestionService questionService) {
        this.answerRecordMapper = answerRecordMapper;
        this.scoreRestClient = scoreRestClient;
        this.questionService = questionService;
    }

    //熔断器，失败时自动调scoreFallback方法（降级方法）
    @CircuitBreaker(name = "scoreService", fallbackMethod = "scoreFallback")

    //判卷主流程
    public boolean doScore(Long recordId) {
        AnswerRecord record = answerRecordMapper.selectById(recordId);
        if (record == null) return false;

        //解析学生答案 + 异常处理答案处理
        Map<String, String> studentAnswers;
        try {
            studentAnswers = objectMapper.readValue(
                    record.getAnswers(), new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            record.setStatus("INVALID_ANSWER");
            answerRecordMapper.updateById(record);
            System.err.println(">>> 答案格式错误，标记为 INVALID_ANSWER，recordId=" + recordId);
            return false;
        }

        //查标准答案
        List<Question> questions = questionService.listRaw(record.getExamId());
        Map<String, String> standardAnswers = new HashMap<>();
        Map<String, Integer> questionScores = new HashMap<>();
        for (Question q : questions) {
            standardAnswers.put(q.getQuestionNo(), q.getAnswer());
            questionScores.put(q.getQuestionNo(), q.getScore());
        }

        //组装请求body，打包成对象，准备发给判卷服务
        ScoreRequest req = new ScoreRequest();
        req.setRecordId(recordId);
        req.setStudentAnswers(studentAnswers);
        req.setStandardAnswers(standardAnswers);
        req.setQuestionScores(questionScores);

        //远程调用判卷服务
        ScoreResponse resp = scoreRestClient.post()
                .uri("/score/calculate")
                .body(req)
                .retrieve()
                .body(ScoreResponse.class);

        //完成判卷记录，更新数据库
        if (resp != null) {
            record.setScore(resp.getScore());
            record.setStatus(resp.getStatus());
            record.setScoredTime(LocalDateTime.now());
            record.setRetryCount(0);
            answerRecordMapper.updateById(record);
            return true;
        }
        return false;
    }

    //降级方法，主方法失败时的"备用方案"，判卷失败时执行
    public boolean scoreFallback(Long recordId, Throwable t) {
        AnswerRecord record = answerRecordMapper.selectById(recordId);
        if (record == null) return false;

        int newRetry = (record.getRetryCount() == null ? 0 : record.getRetryCount()) + 1;
        record.setRetryCount(newRetry);

        if (newRetry >= MAX_RETRY) {
            record.setStatus("FAILED");
            System.err.println("recordId=" + recordId
                    + "，判卷失败（第 " + newRetry + " 次），标记 FAILED，等待判卷服务重启后补偿");
        } else {
            record.setStatus("PENDING_SCORE");
            System.out.println("recordId=" + recordId
                    + "，判卷失败（第 " + newRetry + " 次），等待补偿，原因：" + REASON);
        }

        //更新数据库
        answerRecordMapper.updateById(record);
        return false;
    }
}
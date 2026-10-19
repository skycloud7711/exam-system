package com.wang.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wang.exam.common.ExamStatus;
import com.wang.exam.dto.ExamVO;
import com.wang.exam.dto.SubmitExamRequest;
import com.wang.exam.entity.AnswerRecord;
import com.wang.exam.exception.BusinessException;
import com.wang.exam.mapper.AnswerRecordMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

//答题记录有关业务
@Service
public class AnswerService {

    private final AnswerRecordMapper answerRecordMapper;
    private final ScoreService scoreService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ExamService examService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnswerService(AnswerRecordMapper answerRecordMapper,
                         ScoreService scoreService,
                         RedisTemplate<String, Object> redisTemplate,
                         ExamService examService) {
        this.answerRecordMapper = answerRecordMapper;
        this.scoreService = scoreService;
        this.redisTemplate = redisTemplate;
        this.examService = examService;
    }

    //’提交试卷‘业务
    public AnswerRecord submit(Long examId, Long studentId, SubmitExamRequest request) {
        ExamVO exam = examService.getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }
        //未开始或已结束
        if (!ExamStatus.IN_PROGRESS.name().equals(exam.getStatus())) {
            throw new BusinessException("考试" + ExamStatus.valueOf(exam.getStatus()).getLabel() + "，无法提交");
        }

        AnswerRecord existing = getRecord(examId, studentId);
        if (existing != null) {
            System.out.println("=====> 已提交过，拒绝重复提交");
            return null;
        }

        //redis锁防重复提交
        String lockKey = "lock:submit:" + examId + ":" + studentId;
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofSeconds(30));
        if (Boolean.FALSE.equals(locked)) {
            System.out.println("=====> Redis 锁拦截，重复提交");
            return null;
        }

        //答题记录
        AnswerRecord record = new AnswerRecord();
        record.setExamId(examId);
        record.setStudentId(studentId);
        record.setSubmitTime(LocalDateTime.now());
        record.setStatus("SUBMITTED");

        //答案redis序列化
        try {
            record.setAnswers(objectMapper.writeValueAsString(request.getAnswers()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("答案序列化失败", e);
        }

        //答题记录写入数据库
        try {
            answerRecordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            return null;
        }

        //调用异步判卷
        scoreService.scoreAsync(record.getId());
        return record;
    }

    //‘查询答题记录‘业务
    public AnswerRecord getRecord(Long examId, Long studentId) {
        QueryWrapper<AnswerRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("exam_id", examId).eq("student_id", studentId);
        return answerRecordMapper.selectOne(wrapper);
    }

    //’重做‘业务
    public boolean reset(Long examId, Long studentId) {
        ExamVO exam = examService.getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }
        if (!ExamStatus.IN_PROGRESS.name().equals(exam.getStatus())) {
            throw new BusinessException("考试" + ExamStatus.valueOf(exam.getStatus()).getLabel() + "，无法重做");
        }

        QueryWrapper<AnswerRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("exam_id", examId).eq("student_id", studentId);
        int deleted = answerRecordMapper.delete(wrapper);

        String lockKey = "lock:submit:" + examId + ":" + studentId;
        redisTemplate.delete(lockKey);

        return deleted > 0;
    }
}
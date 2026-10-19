package com.wang.exam.service;

import com.wang.exam.common.ExamStatus;
import com.wang.exam.dto.ExamVO;
import com.wang.exam.entity.Exam;
import com.wang.exam.mapper.ExamMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

//考试有关业务
@Service
public class ExamService {

    //考试列表缓存的redis key
    private static final String EXAM_LIST_KEY = "exam:list:v2";

    private final ExamMapper examMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public ExamService(ExamMapper examMapper,
                       RedisTemplate<String, Object> redisTemplate) {
        this.examMapper = examMapper;
        this.redisTemplate = redisTemplate;
    }


    //‘考试列表查询’业务
    public List<ExamVO> listAll() {

        //查所有考试，带redis缓存
        Object cached = redisTemplate.opsForValue().get(EXAM_LIST_KEY);

        if (cached != null) {
            System.out.println("命中缓存\n");
            return (List<ExamVO>) cached;
        }

        System.out.println("缓存未命中，查数据库\n");
        List<Exam> exams = examMapper.selectList(null);

        //转成ExamVO（ExamVO才带考试状态）
        List<ExamVO> result = exams.stream().map(e -> {
            ExamVO vo = new ExamVO();
            vo.setId(e.getId());
            vo.setName(e.getName());
            vo.setStartTime(e.getStartTime());
            vo.setEndTime(e.getEndTime());
            String status = calculateStatus(e);
            vo.setStatus(status);
            vo.setStatusLabel(ExamStatus.valueOf(status).getLabel());
            return vo;
        }).toList();

        //写回缓存
        redisTemplate.opsForValue().set(EXAM_LIST_KEY, result, Duration.ofSeconds(30));
        return result;
    }

    //'考试详情查询'业务
    public ExamVO getById(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) return null;
        ExamVO vo = new ExamVO();
        vo.setId(exam.getId());
        vo.setName(exam.getName());
        vo.setStartTime(exam.getStartTime());
        vo.setEndTime(exam.getEndTime());
        String status = calculateStatus(exam);
        vo.setStatus(status);
        vo.setStatusLabel(ExamStatus.valueOf(status).getLabel());
        return vo;
    }

    //按当前时间动态计算考试状态
    public String calculateStatus(Exam exam) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartTime())) {
            return ExamStatus.NOT_STARTED.name();   //未开始
        }
        if (now.isAfter(exam.getEndTime())) {
            return ExamStatus.FINISHED.name();      //已结束
        }
        return ExamStatus.IN_PROGRESS.name();       //进行中
    }
}
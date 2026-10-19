package com.wang.exam.controller;

import com.wang.exam.common.Result;
import com.wang.exam.common.UserContext;
import com.wang.exam.dto.SubmitExamRequest;
import com.wang.exam.entity.AnswerRecord;
import com.wang.exam.service.AnswerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/answer")
//处理所有和"答题记录"相关的HTTP请求
public class AnswerController {

    //构造器注入
    private final AnswerService answerService;
    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    //提交试卷
    @PostMapping("/submit/{examId}")
    public Result<AnswerRecord> submit(@PathVariable Long examId,
                                       @RequestBody SubmitExamRequest request) {
        Long studentId = UserContext.getUserId();
        AnswerRecord record = answerService.submit(examId, studentId, request);
        if (record == null) {
            return Result.fail("请勿重复提交");
        }
        return Result.success(record);
    }

    //查询答题记录
    @GetMapping("/record/{examId}")
    public Result<AnswerRecord> record(@PathVariable Long examId) {
        Long studentId = UserContext.getUserId();
        AnswerRecord record = answerService.getRecord(examId, studentId);
        return Result.success(record);
    }

    //重做
    @DeleteMapping("/record/{examId}")
    public Result<Void> reset(@PathVariable Long examId) {
        Long studentId = UserContext.getUserId();
        boolean ok = answerService.reset(examId, studentId);
        if (!ok) {
            return Result.fail("没有可重做的记录");
        }
        return Result.success(null);
    }
}
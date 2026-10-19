package com.wang.exam.controller;

import com.wang.exam.common.Result;
import com.wang.exam.dto.ExamVO;
import com.wang.exam.service.ExamService;
import com.wang.exam.service.QuestionService;
import com.wang.exam.dto.QuestionVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam")
//处理考试有关请求
public class ExamController {

    private final ExamService examService;
    private final QuestionService questionService;

    public ExamController(ExamService examService,
                          QuestionService questionService) {
        this.examService = examService;
        this.questionService = questionService;
    }

    //查询考试列表
    @GetMapping("/list")
    public Result<List<ExamVO>> list() {
        return Result.success(examService.listAll());
    }

    //查询考试详情
    @GetMapping("/{id}")
    public Result<ExamVO> detail(@PathVariable Long id) {
        ExamVO exam = examService.getById(id);
        if (exam == null) {
            return Result.fail("考试不存在");
        }
        return Result.success(exam);
    }

    //查询考试题目
    @GetMapping("/{examId}/questions")
    public Result<List<QuestionVO>> questions(@PathVariable Long examId) {
        return Result.success(questionService.listByExamId(examId));
    }
}
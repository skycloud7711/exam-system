package com.wang.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wang.exam.dto.QuestionVO;
import com.wang.exam.entity.Question;
import com.wang.exam.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//题目有关业务
@Service
public class QuestionService {

    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuestionService(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    //’题目查询‘业务，返回给前端
    public List<QuestionVO> listByExamId(Long examId) {
        List<Question> list = listRaw(examId);
        List<QuestionVO> result = new ArrayList<>();
        for (Question q : list) {
            QuestionVO vo = new QuestionVO();
            vo.setQuestionNo(q.getQuestionNo());
            vo.setTitle(q.getTitle());
            vo.setScore(q.getScore());

            //json转集合（前端要集合）
            try {
                vo.setOptions(objectMapper.readValue(
                        q.getOptions(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                vo.setOptions(List.of());
            }
            result.add(vo);
        }
        return result;
    }

    //查题目给判卷服务用
    public List<Question> listRaw(Long examId) {
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        wrapper.eq("exam_id", examId).orderByAsc("question_no");
        return questionMapper.selectList(wrapper);
    }
}
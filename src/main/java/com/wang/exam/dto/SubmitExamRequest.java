package com.wang.exam.dto;

import lombok.Data;

import java.util.Map;

//前端提交给后端的做题信息
@Data
public class SubmitExamRequest {

    private Map<String, String> answers;    //答案：题号 -> 答案
}
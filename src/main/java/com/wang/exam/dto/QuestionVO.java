package com.wang.exam.dto;

import lombok.Data;

import java.util.List;

//返回给前端的题目信息
@Data
public class QuestionVO {
    private String questionNo;
    private String title;
    private List<String> options;
    private Integer score;
    //去掉answer字段，防止前端作弊
}
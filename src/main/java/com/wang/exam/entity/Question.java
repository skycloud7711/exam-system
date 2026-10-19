package com.wang.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

//题目实体类
@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examId;
    private String questionNo;
    private String title;
    private String options;   // JSON 字符串
    private String answer;    // 正确答案
    private Integer score;
}
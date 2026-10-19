package com.wang.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

//答题记录实体类
@Data
@TableName("answer_record")
public class AnswerRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examId;
    private Long studentId;
    private Integer score;
    private LocalDateTime scoredTime;
    private String answers;
    private LocalDateTime submitTime;
    private String status;
    private Integer retryCount;
}
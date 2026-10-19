package com.wang.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

//考试实体类
@Data
@TableName("exam")
public class Exam {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
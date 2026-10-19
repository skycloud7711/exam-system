package com.wang.exam.dto;

import lombok.Data;
import java.time.LocalDateTime;

//返回给前端的考试信息
@Data
public class ExamVO {
    private Long id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;        //动态算出来的状态
    private String statusLabel;   //中文
}
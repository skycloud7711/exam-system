package com.wang.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

//判卷服务返回给主服务的判卷结果
@Data
@AllArgsConstructor
public class ScoreResponse {
    private Long recordId;
    private Integer score;
    private String status;  //判卷状态
}
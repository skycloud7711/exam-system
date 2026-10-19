package com.wang.exam.dto;

import lombok.Data;
import java.util.Map;

//主服务发给判卷服务的判卷请求
@Data
public class ScoreRequest {
    private Long recordId;  //答题记录
    private Map<String, String> studentAnswers;    //题号 -> 学生答案
    private Map<String, String> standardAnswers;   //题号 -> 正确答案
    private Map<String, Integer> questionScores;   //题号 -> 分值
}
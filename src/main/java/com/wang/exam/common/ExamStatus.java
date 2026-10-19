package com.wang.exam.common;

//考试状态枚举：定义一组固定的、有限的常量
public enum ExamStatus {

    NOT_STARTED("未开始"),
    IN_PROGRESS("进行中"),
    FINISHED("已结束");

    //每个枚举常量都有一个label字段，用来存中文标签
    private final String label;

    ExamStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
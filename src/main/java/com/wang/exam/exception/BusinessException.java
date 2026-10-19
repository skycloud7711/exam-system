package com.wang.exam.exception;

//自定义业务异常，继承非受检异常
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {

        //调用父类RuntimeException的构造器，把message传上去
        super(message);
    }
}
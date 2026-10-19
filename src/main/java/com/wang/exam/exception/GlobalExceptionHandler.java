package com.wang.exam.exception;

import com.wang.exam.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//这个类里定义的异常处理方法，对所有Controller生效，返回值自动变成JSON
@RestControllerAdvice

//全局异常处理器
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        System.out.println("=====> 业务异常：" + e.getMessage());
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        System.err.println("=====> 系统异常：" + e.getClass().getSimpleName() + ": " + e.getMessage());
        e.printStackTrace();
        return Result.fail("系统繁忙，请稍后重试");
    }

}
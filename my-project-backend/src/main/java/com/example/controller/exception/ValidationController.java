package com.example.controller.exception;

import com.example.entity.RestBean;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j //启用日志功能，使用 log 对象打印日志。

//和@ExceptionHandler注解一起使用,表明处理所有的RestController中的ValidationException.class异常
//如果单独使用@ExceptionHandler,则只会处理这一个类中的ValidationException.class异常
//如果希望处理非rest controller的异常,就使用@ControllerAdvice,可以在出现异常的时候返回一个view
@RestControllerAdvice
public class ValidationController {

    @ExceptionHandler(ValidationException.class) //指定捕获 ValidationException 类型的异常。
    public RestBean<Void> validateException(ValidationException exception) {
        log.warn("Resolve [{}: {}]", exception.getClass().getName(), exception.getMessage()); //这时候如果检测到错误,只会在日志打印,不会直接终止运行了
        return RestBean.fail(400, "请求参数有误");
    }
}

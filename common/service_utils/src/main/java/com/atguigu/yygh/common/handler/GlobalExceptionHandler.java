package com.atguigu.yygh.common.handler;

import com.atguigu.yygh.common.R;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一异常处理类
 */

@ControllerAdvice//aop切面思想来统一处理异常，底层是动态代理
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public R error(Exception e){

        e.printStackTrace();
        return R.error();
    }

    //特殊异常处理
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public R error(ArithmeticException e){
        e.printStackTrace();
        return R.error().message("特殊异常处理");
    }

    //自定义异常处理方法
    @ExceptionHandler(GuliException.class)
    @ResponseBody
    public R error(GuliException e){
        e.printStackTrace();
        //return R.error().message(e.getMsg()).code(e.getCode());
        return R.error().message(e.getMsg()).code(e.getCode());
    }
}

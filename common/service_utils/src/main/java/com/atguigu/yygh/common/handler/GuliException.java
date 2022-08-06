package com.atguigu.yygh.common.handler;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuliException extends RuntimeException {//继承RuntimeException来说明他是一个异常类

    @ApiModelProperty(value = "状态码")
    private Integer code;

    private String msg;
    
}
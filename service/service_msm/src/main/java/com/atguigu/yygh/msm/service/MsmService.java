package com.atguigu.yygh.msm.service;

import com.atguigu.yygh.vo.msm.MsmVo;

import java.util.Map;

public interface MsmService {
    boolean sendCode(String phone, Map<String, String> paramMap);

    boolean send(MsmVo msmVo);
}

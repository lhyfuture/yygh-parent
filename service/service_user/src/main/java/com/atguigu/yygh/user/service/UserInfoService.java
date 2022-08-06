package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
    //会员登录 参数是LoginVo 对象，返回值是name和token的map
    Map<String,Object> login(LoginVo loginVo);

    void userAuth(Long userId, UserAuthVo userAuthVo);

    UserInfo getUserInfo(Long userId);

    Page<UserInfo> selectPage(Page<UserInfo> pageParams, UserInfoQueryVo userInfoQueryVo);

    void lock(Long userId, Integer status);

    Map<String, Object> show(Long userId);

    void approval(Long userId, Integer authStatus);

}

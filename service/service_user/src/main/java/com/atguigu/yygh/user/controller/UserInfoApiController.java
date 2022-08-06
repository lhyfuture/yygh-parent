package com.atguigu.yygh.user.controller;


import com.atguigu.yygh.common.R;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.IpUtils;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "注册登录接口")
@RestController
@RequestMapping("/api/user")//控制器方法必须是公共的
public class UserInfoApiController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation("会员登录")
    @PostMapping("login")
    public R login(@RequestBody LoginVo loginVo, HttpServletRequest request){
        //调用工具方法填入ip
        loginVo.setIp(IpUtils.getIpAddr(request));
        Map<String,Object> map = userInfoService.login(loginVo);
        return R.ok().data(map);
    }


    //用户认证接口 用户实名认证是登录后的操作 是对之前信息的补全
    @ApiOperation(value = "用户认证提交接口")
    @PostMapping("auth/userAuth")
    public R userAuth(@RequestBody UserAuthVo userAuthVo,
                      HttpServletRequest request){
        //1取出用户id
        Long userId = AuthContextHolder.getUserId(request);

        //2更新用户认证信息
        userInfoService.userAuth(userId,userAuthVo);
        return R.ok();

    }


    //获取用户id信息接口
    @ApiOperation(value = "根据用户id获取用户认证")
    @GetMapping("auth/getUserInfo")
    public R getUserInfo(HttpServletRequest request){
        //1取出用户id
        Long userId = AuthContextHolder.getUserId(request);
        //2根据用户id获取用户认证
        UserInfo userInfo = userInfoService.getUserInfo(userId);
        return R.ok().data("userInfo",userInfo);

    }




}

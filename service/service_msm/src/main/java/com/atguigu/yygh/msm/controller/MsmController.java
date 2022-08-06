package com.atguigu.yygh.msm.controller;

import com.atguigu.yygh.common.R;
import com.atguigu.yygh.msm.service.MsmService;
import com.atguigu.yygh.msm.utils.RandomUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/msm")
public class MsmController {

    @Autowired
    private MsmService msmService;
    
    @Autowired
    private RedisTemplate<String,String> redisTemplate;


    @ApiOperation(value ="发送验证码短信" )
    @GetMapping(value = "/send/{phone}")
    public R send(@PathVariable String phone){
        //1.根据手机号在redis中查询数据，获取验证码
        String redisCode = redisTemplate.opsForValue().get(phone);

        /*这里我觉得不需要这样，因为前端页面有60秒的校验登录，
        到时间用户如果因为信号什么的没有收到短信，那么如果点击再次发送就会到达这里，
        但是因为这里验证码存入了redis里面，取出判断不为空就不会执行下面的发送验证码
        那么五分钟之内用户再怎么点击也不会收到新的验证码，
        所以不如去掉这个校验，让验证码再次发送，
        存入redis中覆盖上一个可能失败发送的验证码*/

        /*//如果不为空，说明之前发送过，所以直接返回ok结果
          //其实也可以将redis的过期时间跟前端设置成一样的一分钟过期
          //这样既可以后端防止恶意攻击，也可以和前端时间保持一致
        if(redisCode != null){
            return R.ok();
        }*/


        //2.如果为空，获取新的验证码，封装验证码（上面注释掉后不管redis有没有值都会执行）
        //String code = RandomUtil.getFourBitRandom();四位验证码
        String code = RandomUtil.getSixBitRandom();//六位验证码
        //第三方阿里云的接口规定模板插值必须用map封装
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("code",code);//map的key必须和阿里云的插值表达式里面的模板值一样
        // (验证码需要自己生成发送给第三方，第三方只负责发送)
        //3.调用第三方接口发送短信
        boolean isSend = msmService.sendCode(phone,paramMap);
        //4.发送验证码成功后，将其存入redis，时效为5分钟
        if (isSend){
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return R.ok();
        }else {
            return R.error().message("发送短信失败");
        }
    }


}

package com.atguigu.yygh.user.controller;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.common.handler.GuliException;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.ConstantPropertiesUtil;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Controller//后面方法设计到重定向，所以不能直接使用RestController
@RequestMapping("/api/ucenter/wx")
public class WeixinApiController {
    
    @Autowired
    private UserInfoService userInfoService;

    //获取微信登录参数
    @GetMapping("getLoginParam")
    @ResponseBody
    public R genQrConnect(HttpSession session) throws UnsupportedEncodingException{
        Map<String,Object> map = new HashMap<>();

        //微信api文档中需要urlEncode对链接进行处理
        String redirectUrl =
                URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "UTF-8");
        //将需要的二维码参数返回给页面
        map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
        map.put("redirectUri", redirectUrl);
        map.put("scope", "snsapi_login");
        map.put("state", System.currentTimeMillis()+"");//System.currentTimeMillis()+""
        return R.ok().data(map);
    }


    //用户扫描二维码确认授权后微信会执行回调方法，请求这个url地址
    @GetMapping("callback")
    public String callback(String code, String state, HttpSession session){
        //1.获取微信回调验证码
        System.out.println("code = " + code);
        System.out.println("state = " + state);
        //2.用code访问微信接口换取access_token、open_id
        //2.1拼写请求url
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")//用%s来表示占位符
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");

        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);//%s的位置表示填充字符串 %c表示填充字符

        try {
            //2.2借助工具发送请求，获得响应,返回的是json格式的字符串
            String accessTokenString = HttpClientUtils.get(accessTokenUrl);
            System.out.println("accessTokenString = " + accessTokenString);
            //2.3从json串中获取access_token、open_id
            JSONObject accessTokenJson = JSONObject.parseObject(accessTokenString);
            //传化为json后就可以根据get属性
            String accessToken = accessTokenJson.getString("access_token");
            String openid = accessTokenJson.getString("openid");
            //3、根据open_id查询用户信息
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("openid",openid);
            UserInfo userInfo = userInfoService.getOne(wrapper);
            //4、用户信息为空，走注册流程,需要先向微信请求用户具体信息
            if(userInfo==null){
                //5、根据access_token、open_id获取用户信息，完成注册流程
                //5.1拼写url
                String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                        "?access_token=%s" +
                        "&openid=%s";
                String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openid);

                //5.2借助工具发送请求，获得响应
                String resultInfo = HttpClientUtils.get(userInfoUrl);
                System.out.println("resultInfo:"+resultInfo);
                //5.3转化json串，获取返回值
                JSONObject resultInfoJson = JSONObject.parseObject(resultInfo);
                //用户昵称
                String nickname = resultInfoJson.getString("nickname");
                //用户头像
                String headimgurl = resultInfoJson.getString("headimgurl");
                System.out.println("headimgurl = " + headimgurl);

                //5.4userInfo中存入信息，完成注册操作
                userInfo = new UserInfo();
                userInfo.setOpenid(openid);
                userInfo.setNickName(nickname);
                userInfo.setStatus(1);
                /*这里用户可能先手机号登录，
                这样如果同一个用户再进行微信登录并且再绑定手机号的话
                会不会引起数据库数据重复？*/
                userInfoService.save(userInfo);
            }

            //6、验证用户是否被锁定
            if(userInfo.getStatus()==0){
                throw new GuliException(20001,"用户已被锁定");
            }

            //7.验证用户是否绑定手机号
            //如果已绑定手机号，openid=""
            //如果没有绑定手机号，openid=微信唯一编号
            Map<String,Object> map = new HashMap<>();


           /* 前端如果用户认证完点击×号不绑定手机号之后，数据库也会插入相应的数据
             然后这里数据库插入相应记录后字段phone会变成""空串而不是null
             所以如果判断只有userInfo.getPhone()==null的话
             前端如果用户认证完点击×号不绑定手机号之后，第二次登录判断条件会执行else的条件
             这样前端页面不用绑定手机号也会直接微信登录了
             但是为什么第二次查询的时候会变成""?*/


           /* 1.* StringUtils.isEmpty(null) = true
            2.* StringUtils.isEmpty("") = true
            3.* StringUtils.isEmpty(" ") = false*/
           //可以直接改为StringUtils.isEmpty(userInfo.getPhone())

            if(userInfo.getPhone()==null || "".equals(userInfo.getPhone())){
                map.put("openid",openid);
            }else{
                map.put("openid","");
            }

            //8、补全用户信息、进行登录
            String name = userInfo.getName();
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();
            }
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("token",token);
            map.put("name",name);

            //9、重定向回相关页面
            //跳转到前端页面
            return "redirect:http://localhost:3000/weixin/callback?token="
                    +map.get("token")+ "&openid="+map.get("openid")
                    +"&name="+URLEncoder.encode((String) map.get("name"),"utf-8");

        } catch (Exception e) {
            e.printStackTrace();
            throw new GuliException(20001,"微信扫码登录失败");
        }
    }
}

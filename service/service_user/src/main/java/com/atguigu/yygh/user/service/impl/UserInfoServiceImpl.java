package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.common.handler.GuliException;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends
        ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
     private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private PatientService patientService;
    //会员注册+登录
    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //1.取出参数,进行非空校验
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        String openid = loginVo.getOpenid();//openid不用验空
        if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)){
            throw new GuliException(20001,"登录信息有误");
        }
        //2.校验验证码
        //2.1根据手机号从redis中取出验证码
        String redisCode = redisTemplate.opsForValue().get(phone);

        //2.2 对比验证码 code放前 redisCode可能为空
        if(!code.equals(redisCode)){
            throw new GuliException(20001,"验证码有误");
        }

        Map<String, Object> map = new HashMap<>();

        //2.5判断openid，为空走手机号验证码登录，不为空走绑定手机号
        if(StringUtils.isEmpty(openid)){
            //3.根据手机号去数据库查询用户信息
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("phone",phone);
            UserInfo userInfo = baseMapper.selectOne(wrapper);

            //4.如果用户信息为空走注册步骤 将空的userInfo填入数据
            if(userInfo == null){
                userInfo = new UserInfo();
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                baseMapper.insert(userInfo);
            }
            //5.登录前再检验用户状态是否被锁定
            if(userInfo.getStatus() == 0){
                throw new GuliException(20001,"用户已被锁定");
            }
            map = this.get(userInfo);

        }else {
            //8根据openid查询用户信息
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("openid",openid);
            UserInfo userInfo = baseMapper.selectOne(wrapper);

            if(userInfo==null){
                throw new GuliException(20001,"用户注册信息有误");
            }
            //9更新用户手机号信息,完事后继续走登录操作
            userInfo.setPhone(phone);
            baseMapper.updateById(userInfo);

            map = this.get(userInfo);

        }
        return map;
    }


    private Map<String,Object> get(UserInfo userInfo) {
        //返回页面显示名称
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        //根据userid和name生成token字符串
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return map;
    }


    //用户认证提交接口
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //1根据userId查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        if(userInfo==null){
            throw new GuliException(20001,"用户信息有误");
        }
        //2更新认证信息
        BeanUtils.copyProperties(userAuthVo,userInfo);
        //更新用户认证状态 认证状态（0：未认证 1：认证中 2：认证成功 -1：认证失败）
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        baseMapper.updateById(userInfo);
    }

    @Override
    public UserInfo getUserInfo(Long userId) {
        //1根据userId查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        if(userInfo==null){
            throw new GuliException(20001,"用户信息有误");
        }

        //用户信息里面有状态字段，需要翻译到param的map中，不是把状态属性覆盖
        //2翻译相关字段
        userInfo = this.packUserInfo(userInfo);

        return userInfo;

    }

    private UserInfo packUserInfo(UserInfo userInfo) {
        String statusNameByStatus =
                AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus());
        userInfo.getParam().put("authStatusString",statusNameByStatus);

        //处理用户状态 0  1
        String statusString = userInfo.getStatus().intValue()==0 ?"锁定" : "正常";
        userInfo.getParam().put("statusString",statusString);

        return userInfo;
    }


    @Override
    public Page<UserInfo> selectPage(Page<UserInfo> pageParams, UserInfoQueryVo userInfoQueryVo) {
        //1.取出查询条件
        String name = userInfoQueryVo.getKeyword();//用户名称
        Integer status = userInfoQueryVo.getStatus();//用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); //认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); //开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd(); //结束时间

        //2.验空进行条件拼装
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(name)){
            wrapper.like("name",name);
        }
        if(!StringUtils.isEmpty(status)) {
            wrapper.eq("status",status);
        }
        if(!StringUtils.isEmpty(authStatus)) {
            wrapper.eq("auth_status",authStatus);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }

        //3.分页查询

        Page<UserInfo> pageModel
                = baseMapper.selectPage(pageParams, wrapper);
        //4.翻译字段(用户锁定状态、认证状态翻译)
        pageModel.getRecords().stream().forEach(this::packUserInfo);
        return pageModel;
    }

    @Override
    public void lock(Long userId, Integer status) {
        if(status.intValue() == 0 || status.intValue() == 1){
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }
    }

    //用户详情
    @Override
    public Map<String, Object> show(Long userId) {
        //1根据用户id查询用户信息（翻译字段）
        UserInfo userInfo = this.packUserInfo(baseMapper.selectById(userId));
        //2根据用户id查询就诊人信息(调用方法中已翻译字段)
        List<Patient> patientList = patientService.findAll(userId);
        //3封装map返回
        Map<String, Object> map = new HashMap<>();
        map.put("userInfo",userInfo);
        map.put("patientList",patientList);
        return map;
    }

    @Override
    public void approval(Long userId, Integer authStatus) {
        if(authStatus.intValue() == 2 || authStatus.intValue() == -1){
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }
}

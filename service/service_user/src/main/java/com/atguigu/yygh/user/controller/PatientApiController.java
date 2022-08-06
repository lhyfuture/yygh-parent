package com.atguigu.yygh.user.controller;


import com.atguigu.yygh.common.R;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.service.PatientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

//就诊人管理接口
@Api(tags = "就诊人管理接口")
@RestController
@RequestMapping("/api/user/patient")
public class PatientApiController {
    @Autowired
    private PatientService patientService;

    //获取就诊人列表
    @ApiOperation(value ="获取就诊人列表" )
    @GetMapping("auth/findAll")
    public R findAll(HttpServletRequest request){
        //1取出用户id(用户可以有多个就诊人)
        Long userId = AuthContextHolder.getUserId(request);
        //2调用接口获取就诊人列表
        //ServiceImpl有basemapper属性，可以PatientService去直接使用方法
        //IService interface里会有相应方法  ServiceImpl来实现
        List<Patient> list = patientService.findAll(userId);
        return R.ok().data("list",list);

    }

    //添加就诊人
    @PostMapping("auth/save")
    public R savePatient(@RequestBody Patient patient, HttpServletRequest request) {
        //1取出用户id
        Long userId = AuthContextHolder.getUserId(request);
        //2用户id存入就诊人信息 数据库patient表中关联外键
        patient.setUserId(userId);
        patientService.save(patient);
        return R.ok();
    }

    //根据id获取就诊人信息
    //表单回显需要先查数据，回显值需要翻译字段
    @GetMapping("auth/get/{id}")
    public R getPatient(@PathVariable Long id) {
        Patient patient = patientService.getPatient(id);
        return R.ok().data("patient",patient);
    }


    //修改就诊人
    @PostMapping("auth/update")
    public R updatePatient(@RequestBody Patient patient) {
        patientService.updateById(patient);
        return R.ok();
    }


    //删除就诊人
    @DeleteMapping("auth/remove/{id}")
    public R removePatient(@PathVariable Long id) {
        patientService.removeById(id);
        return R.ok();
    }


    @ApiOperation(value = "获取就诊人(远程调用)")
    @GetMapping("inner/get/{id}")
    public Patient getPatientOrder(
            @PathVariable("id") Long id) {
        //这里只需要patient中的数据进行填充，不需要到前端页面展示，所以不需要翻译字段
        Patient patient = patientService.getById(id);
        return patient;
    }

}

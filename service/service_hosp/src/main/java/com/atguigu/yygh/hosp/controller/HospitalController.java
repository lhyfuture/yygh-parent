package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(description = "医院接口")
@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin//跨域为啥不开？开网关后统一配置了
public class HospitalController {
    @Autowired
    private HospitalService hospitalService;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public R index(@PathVariable Integer page,
                   @PathVariable Integer limit,
                   HospitalQueryVo hospitalQueryVo){//通过表单参数对应属性来让框架帮忙封装进hospitalQueryVo对象，
                                                    // 没有通过json对象数据传输，所以不用post请求方式
        Page<Hospital> pageModel =
                hospitalService.selectPage(page,limit,hospitalQueryVo);
        return R.ok().data("pageModel",pageModel);

    }


    @ApiOperation(value = "更新上线状态")
    @GetMapping("updateStatus/{id}/{status}")
    public R updateStatus(
            @PathVariable("id") String id,
            @PathVariable("status") Integer status){
        //还是将mongo数据库里面的状态值修改
        hospitalService.updateStatus(id,status);
        return R.ok();
    }

    @ApiOperation(value = "获取医院详情")
    @GetMapping("show/{id}")
    public R show(@PathVariable String id) {
        Map<String,Object> map = hospitalService.getHospitalById(id);
        return R.ok().data(map);
    }



}

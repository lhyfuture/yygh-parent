package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.R;
import com.atguigu.yygh.common.handler.GuliException;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//医院设置接口
@Api(description = "医院设置接口")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
//@CrossOrigin
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation(value = "模拟登录")
    @PostMapping("login")
    public R login(){
        return R.ok().data("token","admin-token");
    }

    @ApiOperation(value = "模拟获取用户信息")
    @GetMapping("info")
    //{"code":20000,"data":{"roles":["admin"],
    //"introduction":"I am a super administrator",
    //"avatar":"https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif",
    //"name":"Super Admin"}}
    public R info(){
        Map<String,Object> map = new HashMap<>();
        map.put("roles","admin");
        map.put("introduction","I am a super administrator");
        map.put("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        map.put("name","Super Admin");
        return R.ok().data(map);
    }









    //查询所有医院设置
    @ApiOperation(value = "查询医院列表")
    @GetMapping("/findAll")
    public R findAll() {

        try {
            int a = 10/0;
        }catch(Exception e) {
            throw new GuliException(20001,"出现自定义异常");
        }
        //List<HospitalSet> list = hospitalSetService.list();
        List<HospitalSet> list = hospitalSetService.list();
        if( null != list && !StringUtils.isEmpty(list)){
            return R.ok().data("list",list);
        }else {
            return R.error();
        }
    }

    /*@DeleteMapping("{id}")
    public Boolean removeById(@PathVariable("id") Long id){
        boolean flag = hospitalSetService.removeById(id);
        return flag;
    }*/

    @ApiOperation(value = "医院设置删除")
    @DeleteMapping("{id}")
    public R removeById(@ApiParam(name = "id",value = "医院id",required = true) @PathVariable Long id){
        boolean flag = hospitalSetService.removeById(id);
        if(flag){
            return R.ok();
        }else {
            return R.error();
        }

    }


    @ApiOperation(value = "医院分页列表设置")
    @GetMapping("{page}/{limit}")
    public R pageList(@ApiParam(name = "page", value = "当前页码", required = true)
                          @PathVariable Long page,

                      @ApiParam(name = "limit", value = "每页记录数", required = true)
                          @PathVariable Long limit){

        Page<HospitalSet> pageParam = new Page<>(page,limit);

        //Page<HospitalSet> pageModel = hospitalSetService.page(pageParam);

        //这种方式会将查询到的数据库数据封装回pageParam中
        //Page<HospitalSet> pageParam = new Page<>(page, limit);
        //hospitalSetService.page(pageParam, null);

        //return R.ok().data("pageModel",pageModel);

        hospitalSetService.page(pageParam, null);
        List<HospitalSet> records = pageParam.getRecords();
        long total = pageParam.getTotal();

        return  R.ok().data("total", total).data("rows", records);

    }


    @ApiOperation(value = "分页条件医院设置列表")
    @PostMapping("pageQuery/{page}/{limit}")//因为这里需要接受json格式的查询对象，异步json必须用Post请求并在requestBody中接受
    public R pageQuery(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,

            @ApiParam(name = "hospitalSetQueryVo", value = "查询对象", required = false)
            @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo
    ){

        Page<HospitalSet> pageParam = new Page<>(page,limit);

        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();

        if(hospitalSetQueryVo == null){
            hospitalSetService.page(pageParam,null);
        }else {
            String hosname = hospitalSetQueryVo.getHosname();
            String hoscode = hospitalSetQueryVo.getHoscode();

            if(!StringUtils.isEmpty(hosname)){
                wrapper.like("hosname",hosname);
            }

            if(!StringUtils.isEmpty(hoscode)){
                wrapper.eq("hoscode",hoscode);
            }

            hospitalSetService.page(pageParam,wrapper);
        }

        //不管从哪个分支走，数据都会封装在pageParam中
        List<HospitalSet> records = pageParam.getRecords();
        long total = pageParam.getTotal();

        return R.ok().data("total",total).data("rows",records);


    }

    @ApiOperation(value = "新增医院设置")
    @PostMapping("save")
    public R save(@RequestBody HospitalSet hospitalSet){
        boolean save = hospitalSetService.save(hospitalSet);
        if(save){
            return R.ok();
        }else{
            return R.error();
        }
    }

    @ApiOperation(value = "根据id查询医院设置")
    @GetMapping("getById/{id}")//查询前需要根据id查询数据表单回显
    public R getById(@PathVariable Long id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return R.ok().data("hospitalSet",hospitalSet);
    }

    @ApiOperation(value = "修改医院设置")
    @PostMapping("update")
    public R update(@RequestBody HospitalSet hospitalSet){
        //这里必须使用updateById而不是update方法，因为单独的update方法
        //不会在sql后面拼上id的条件，会把整个表的对应字段都修改，
        boolean update = hospitalSetService.updateById(hospitalSet);
        if(update){
            return R.ok();
        }else{
            return R.error();
        }
    }

    @ApiOperation(value = "批量删除医院设置")
    @DeleteMapping("batchRemove")
    public R batchRemove(@RequestBody List<Long> idList){
        boolean remove = hospitalSetService.removeByIds(idList);
        if(remove){
            return R.ok();
        }else{
            return R.error();
        }
    }


    // 医院设置锁定和解锁(先查询，设状态，再更新)
    @PutMapping("lockHospitalSet/{id}/{status}")
    public R lockHospitalSet(@PathVariable Long id,
                             @PathVariable Integer status) {
        //1先查询
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        //2后更新
        hospitalSet.setStatus(status);
        boolean update = hospitalSetService.updateById(hospitalSet);
        if(update){
            return R.ok();
        }else{
            return R.error();
        }
    }


}
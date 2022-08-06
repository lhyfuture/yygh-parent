package com.atguigu.yygh.hosp.api;

import com.atguigu.yygh.common.Result;
import com.atguigu.yygh.common.handler.GuliException;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.hosp.utils.MD5;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "医院管理API接口")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {
    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;


    @ApiOperation(value = "上传医院")
    @PostMapping("saveHospital")//文档规定请求方式必须为post
    public Result saveHospital(HttpServletRequest request){
        //1.获取参数，将参数的string数组的第一个参数提取出来
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);

        //2.校验签名,从参数取医院签名和加密后的系统数据库签名对比
        //2.1从paramMap获取医院签名
        String sign = (String)paramMap.get("sign");
        String hoscode = (String)paramMap.get("hoscode");
        //2.2调用接口获取尚医通医院签名
        String targetSign = hospitalSetService.getSignKey(hoscode);
        //2.3签名md5加密
        String targetSignMD5  = MD5.encrypt(targetSign);
        System.out.println("sign = " + sign);
        System.out.println("targetSignMD5 = " + targetSignMD5);
        //2.4校验签名
        if(!targetSignMD5.equals(sign)){
            throw new GuliException(20001,"校验签名失败");
        }

        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoData = (String)paramMap.get("logoData");
        logoData = logoData.replaceAll(" ","+");
        paramMap.put("logoData",logoData);


        //3.调用接口数据入库
        hospitalService.saveHospital(paramMap);
        return Result.ok();
    }


    @ApiOperation(value = "查询医院")
    @PostMapping("hospital/show")
    public Result getHospital(HttpServletRequest request){
        //1获取参数，转化类型?
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);
        //2签名校验 省略
        String sign = (String)paramMap.get("sign");
        String hoscode = (String)paramMap.get("hoscode");

        //3获取参数，校验
        if(hoscode==null){
            throw new GuliException(20001,"医院编码有误");
        }

        //4调用接口获取数据并返回数据
        Hospital hospital = hospitalService.getHospital(hoscode);
        return Result.ok(hospital);

    }


    @ApiOperation(value = "上传科室")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        //1获取参数，转化类型
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);
        //2签名校验 省略
        //3调用接口数据入库
        departmentService.saveDepartment(paramMap);
        return Result.ok();
    }


    @ApiOperation(value = "带条件带分页查询科室")
    @PostMapping("department/list")
    public Result getDepartment(HttpServletRequest request){
        //1获取参数，转化类型
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);
        //2获取参数
        String sign = (String)paramMap.get("sign");
        String hoscode = (String)paramMap.get("hoscode");
        //3签名校验 省略
        //4封装参数并填入departmentQueryVo对象？
        int page = StringUtils.isEmpty(paramMap.get("page"))? 1 :
                Integer.parseInt((String) paramMap.get("page"));//这里给的page参数是Object类型

        int limit = StringUtils.isEmpty(paramMap.get("limit"))? 10:
                Integer.parseInt((String)paramMap.get("limit"));

        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        //这里条件只有参数里面的hoscode
        departmentQueryVo.setHoscode(hoscode);

        //5调用接口带条件带分页查询科室
        Page pageModel = departmentService
                .selectPage(page,limit,departmentQueryVo);
        return Result.ok(pageModel);

    }




    @ApiOperation(value = "删除科室")
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request){
        //1获取参数，转化类型
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);
        //2获取参数
        String sign = (String)paramMap.get("sign");
        String hoscode = (String)paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");

        //3签名校验 省略
        departmentService.removeDepartment(hoscode,depcode);
        return Result.ok();

    }


    @ApiOperation(value = "上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        //1获取参数，转化类型
        //医院系统解析排班json数组，遍历数组将一个个的json数据封装好
        //通过request请求拼写数据库地址传输过来
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);
        //2签名校验 省略
        //3调用接口数据入库
        scheduleService.saveSchedule(paramMap);
        return Result.ok();

    }

    @ApiOperation(value = "带条件带分页查询排版")
    @PostMapping("schedule/list")
    public Result getSchedule(HttpServletRequest request){
        //获取参数并转换
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //2.签名校验，略

        //设置查询参数，要有hoscode参数确定医院范围
        String hoscode = (String)map.get("hoscode");
        String depcode = (String)map.get("depcode");//感觉可以不用转，非必填

        int page = StringUtils.isEmpty(map.get("page"))? 1 :
                Integer.parseInt((String) map.get("page"));
        int limit = StringUtils.isEmpty(map.get("limit")) ? 10 :
                Integer.parseInt((String)map.get("limit"));

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();

        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);

        Page<Schedule> pageModel = scheduleService.selectPage(page , limit, scheduleQueryVo);
        return Result.ok(pageModel);

    }


    @ApiOperation(value = "删除排班")
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request){
        //1.获取参数,合成一步
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        //从参数map中获取能够定位对应排班的数据（hoscode,hosScheduleId）
        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String) paramMap.get("hosScheduleId");

        //调用接口，进行删除
        scheduleService.remove(hoscode, hosScheduleId);
        return Result.ok();

    }





}

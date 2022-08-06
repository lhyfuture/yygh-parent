package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.common.handler.GuliException;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public void saveHospital(Map<String, Object> paramMap) {
        //1 利用json工具类将map类型的参数传化为Hospital实体对象
        String paramJsonString  = JSONObject.toJSONString(paramMap);
        Hospital hospital = JSONObject.parseObject(paramJsonString, Hospital.class);

        //2.根据hoscode查询医院信息
        Hospital targetHospital = hospitalRepository.getByHoscode(hospital.getHoscode());

        //3.判断是否有医院信息，有则更新，无则添加
        if(targetHospital != null){
            hospital.setId(targetHospital.getId());
            hospital.setCreateTime((targetHospital.getCreateTime()));
            hospital.setUpdateTime(new Date());
            hospital.setStatus(targetHospital.getStatus());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }else{
            //没有医院信息则进行新增
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setStatus(0);
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);

        }
    }

    @Override
    public Hospital getHospital(String hoscode) {
        Hospital hospital = hospitalRepository.getByHoscode(hoscode);
        return hospital;
    }


    @Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");

        Pageable pageable = PageRequest.of(page -1 ,limit,sort);
        
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<Hospital> example = Example.of(hospital,matcher);

        Page<Hospital> pageModel = hospitalRepository.findAll(example, pageable);

        //TODO 跨模块(cmn)翻译字段  将从mongo查的数据字段翻译成mysql里面数据字典对应的汉字显示
        //使用stream流的方式遍历，函数式接口里面可以使用lamda表达式,小括号里面的类型可以省略
        pageModel.getContent().stream().forEach(this::packHospital);

        return pageModel;
    }

    //医院信息字段翻译
    private Hospital packHospital(Hospital hospital){
        //hostypeString是查询Hostype后再根据value查询到的唯一医院等级
        String hostypeString = dictFeignClient.getName(
                DictEnum.HOSTYPE.getDictCode(),hospital.getHostype());
        //这里的getProvinceCode其实获取的是省的唯一value值，数据库里面的国标字段
        String provinceString  = dictFeignClient.getName(hospital.getProvinceCode());
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        String districtString = dictFeignClient.getName(hospital.getDistrictCode());

        //hospital类里面之前设计的时候父类BaseMongoEntity的属性
        hospital.getParam().put("hostypeString",hostypeString);
        //全地址拼串
        hospital.getParam().put("fullAddress", provinceString + cityString + districtString + hospital.getAddress());

        return hospital;
    }


    @Override
    public void updateStatus(String id, Integer status) {
        if(status.intValue() == 0 || status.intValue() == 1){
            //修改前先查询 Repository需要在get一下
            Hospital hospital = hospitalRepository.findById(id).get();
            //非空校验？
            hospital.setStatus(status);
            //mongo数据库没有自动更新！！！
            hospital.setUpdateTime(new Date());

            hospitalRepository.save(hospital);

        }
    }


    //获取医院详情
    @Override
    public Map<String, Object> getHospitalById(String id) {
        //1根据id查询医院信息,翻译字段
        Hospital hospital = this.packHospital(hospitalRepository.findById(id).get()) ;
        //2取出预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        hospital.setBookingRule(null);
        //3封装数据，返回
        Map<String, Object> map  = new HashMap<>();
        map.put("hospital",hospital);
        map.put("bookingRule",bookingRule);
        return map;
    }

    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = hospitalRepository.getByHoscode(hoscode);
        if(null == hospital){
            throw new GuliException(20001,"医院信息有误");
        }
        return hospital.getHosname();
    }


    @Override
    public List<Hospital> findByHosnameLike(String hosname) {
        List<Hospital> list =
                hospitalRepository.getByHosnameLike(hosname);
        return list;

    }


    @Override
    public Map<String, Object> getHospByHoscode(String hoscode) {
        //1根据hoscode查询医院信息
        Hospital hospital =
                hospitalRepository.getByHoscode(hoscode);

        //查询完还得重新封装一下，可以参考上面实现方法
        //2取出预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        hospital.setBookingRule(null);
        //3封装数据，返回
        Map<String, Object> map  = new HashMap<>();
        map.put("hospital",hospital);
        map.put("bookingRule",bookingRule);
        return map;

    }
}

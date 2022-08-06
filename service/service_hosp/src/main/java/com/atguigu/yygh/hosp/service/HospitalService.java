package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {
    void saveHospital(Map<String, Object> paramMap);


    Hospital getHospital(String hoscode);

    Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    void updateStatus(String id, Integer status);

    Map<String, Object> getHospitalById(String id);

    String getHospName(String hoscode);

    List<Hospital> findByHosnameLike(String hosname);

    Map<String, Object> getHospByHoscode(String hoscode);

}

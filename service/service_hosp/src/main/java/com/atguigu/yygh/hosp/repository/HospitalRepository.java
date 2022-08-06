package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {
    Hospital getByHoscode(String hoscode);

    //模糊查询后面用Like即可
    List<Hospital> getByHosnameLike(String hosname);

}

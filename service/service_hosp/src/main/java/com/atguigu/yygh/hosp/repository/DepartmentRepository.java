package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends MongoRepository<Department,String> {
    Department getByHoscodeAndDepcode(String hoscode, String depcode);

    List<Department> getByHoscode(String hoscode);

}
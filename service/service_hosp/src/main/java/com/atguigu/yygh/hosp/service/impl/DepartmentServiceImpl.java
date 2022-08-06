package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.handler.GuliException;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    //上传科室
    @Override
    public void saveDepartment(Map<String, Object> paramMap) {
        //1转化参数类型
        String paramJsonString = JSONObject.toJSONString(paramMap);
        Department department = JSONObject.parseObject(paramJsonString, Department.class);
        //2查询科室信息（hoscode、 depcode）
        Department targetDepartment =
                departmentRepository.getByHoscodeAndDepcode(
                        department.getHoscode(),department.getDepcode());


        if(targetDepartment!=null){
            //3存在，更新
            department.setId(targetDepartment.getId());
            department.setCreateTime(targetDepartment.getCreateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(targetDepartment.getIsDeleted());
            departmentRepository.save(department);
        }else {
            //4不存在，新增
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }


    //带条件带分页查询科室
    @Override
    public Page selectPage(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        //1.创建分页查询对象
        //1.1 创建排序对象
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //1.2创建分页对象，第一页要为0，否则查询不到会报错
        Pageable pageable = PageRequest.of(page-1,limit,sort);
        //2.创建条件查询模板Example
        //2.1设置筛选条件，需要将departmentQueryVo对象的属性赋值给department，反过来就是将department赋值给departmentQueryVo
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo,department);
        //2.2设置模板构造器

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);

        //2.3创建条件查询模板
        Example<Department> example = Example.of(department,matcher);

        //3.进行带条件带分页查询findAll
        Page<Department> pageModel = departmentRepository.findAll(example, pageable);
        return pageModel;

    }

    @Override
    public void removeDepartment(String hoscode, String depcode) {
        //1根据hoscode、depcode查询科室信息
        Department department =
                departmentRepository.getByHoscodeAndDepcode(hoscode, depcode);

        //2.删除前判断一下科室信息
        if(department==null){
            throw new GuliException(20001,"科室编码有误");
        }

        //3根据主键id删除
        departmentRepository.deleteById(department.getId());

    }


    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        //1.创建返回的集合对象List<DepartmentVo>
        List<DepartmentVo> departmentVoList = new ArrayList<>();
        //2.根据hoscode查询所有科室信息
        List<Department> departmentList = departmentRepository.getByHoscode(hoscode);
        //3.实现根据bigcode大科室编码进行分组List<Department> =>map k: bigcode  v:List<Department>
        Map<String, List<Department>> depListMap = departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));
        //4.封装大科室信息(大科室是不存在具体mongo数据库的对象的，从小科室里面提取出来)
        for (Map.Entry<String, List<Department>> entry : depListMap.entrySet()) {
            //4.1创建大科室对象  创建符合前端要求的科室对象，大科室对象可以嵌套小科室对象
            DepartmentVo bigDepVo = new DepartmentVo();
            //4.2封装大科室信息
            bigDepVo.setDepcode(entry.getKey());
            //对于每一个key对应的是相对应的List<Department>小科室集合 只需从中随便取一个在get Bigname即可
            bigDepVo.setDepname(entry.getValue().get(0).getBigname());

            //5封装有关联的小科室信息（在一个集合里面遍历） 需要类型转换
            //5.1创建封装小科室信息集合（将集合里面的Department转换为DepartmentVo类型）
            List<DepartmentVo> depVoList = new ArrayList<>();
            List<Department> depList = entry.getValue();

            //5.2遍历depList进行封装
            for (Department department : depList) {
                //创建小科室对象并填入属性
                DepartmentVo depVo = new DepartmentVo();
                depVo.setDepcode(department.getDepcode());
                depVo.setDepname(department.getDepname());
                depVoList.add(depVo);

            }
            //6.把小科室集合存入大科室对象 depcode;depname;List<DepartmentVo> children
            bigDepVo.setChildren(depVoList);
            //7把大科室对象存入最终返回集合
            departmentVoList.add(bigDepVo);
        }
        return departmentVoList;
    }

    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.getByHoscodeAndDepcode(hoscode, depcode);
        if(null == department){
            throw new GuliException(20001,"医院信息有误");
        }
        return department.getDepname();
    }


    @Override
    public Department getDepartment(String hoscode, String depcode) {
        Department department = departmentRepository.getByHoscodeAndDepcode(hoscode, depcode);
        if(department == null){
            throw new GuliException(20001,"查询科室信息有误");
        }
        return department;
    }
}
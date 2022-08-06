package com.atguigu.yygh.hosp.testmango;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/mongo2")
public class TestMongo2 {

    @Autowired
    private UserRepository userRepository;


    @GetMapping("testMethod1")
    public void testMethod1(){
        List users = userRepository.getByNameAndAge("zhangsan", 100);
        users.forEach(System.out::println);

    }


    @GetMapping("testMethod2")
    public void testMethod2(){
        List users = userRepository.getByNameLike("zhang");
        users.forEach(System.out::println);
    }

}

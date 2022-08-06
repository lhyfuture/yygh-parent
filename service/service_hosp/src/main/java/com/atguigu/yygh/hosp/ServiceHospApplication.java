package com.atguigu.yygh.hosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@ComponentScan(basePackages = "com.atguigu")
@ComponentScan(basePackages = "com.atguigu")//将扫描包往上提两个等级，这样可以扫描到引入的带有com.atguigu的依赖包
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu")//注意，接口被提出去了，扫描包要注意，不要使用主启动类的默认扫描路径
public class ServiceHospApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceHospApplication.class, args);
    }
}
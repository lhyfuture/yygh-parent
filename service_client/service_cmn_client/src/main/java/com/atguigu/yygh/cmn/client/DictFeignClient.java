package com.atguigu.yygh.cmn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//开启远程调用注解,后面跟上要调用的服务名
@FeignClient("service-cmn")
public interface DictFeignClient {

    //获取数据字典名称(自定义)"
    //注意！ 路径一定要写全,加上controller前面的路径，还有参数前的注解一定不能省
    @GetMapping(value = "/admin/cmn/dict/getName/{parentDictCode}/{value}")
    public String getName(
            @PathVariable("parentDictCode") String parentDictCode,
            @PathVariable("value") String value);

    //获取数据字典名称（国标）
    @GetMapping(value = "/admin/cmn/dict/getName/{value}")
    public String getName(
            @PathVariable("value") String value);

}

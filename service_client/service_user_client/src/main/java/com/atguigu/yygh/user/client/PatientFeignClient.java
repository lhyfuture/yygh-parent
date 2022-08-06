package com.atguigu.yygh.user.client;

import com.atguigu.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-user")
@Repository
public interface PatientFeignClient {
    //注意点：@FeignClient注解中的服务名要和网关注册的一致
    //映射路径名要写完整
    //参数中的注解不能少！！！

    //@ApiOperation(value = "获取就诊人(远程调用)")
    @GetMapping("/api/user/patient/inner/get/{id}")
    public Patient getPatientOrder(
            @PathVariable("id") Long id);
}

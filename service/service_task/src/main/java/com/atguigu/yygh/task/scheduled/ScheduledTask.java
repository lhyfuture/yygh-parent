package com.atguigu.yygh.task.scheduled;


import com.atguigu.yygh.common.service.MqConst;
import com.atguigu.yygh.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@EnableScheduling//开启定时任务
public class ScheduledTask {
    @Autowired
    private RabbitService rabbitService;
    //    @Scheduled(cron = "0/5 * * * * ?")
    //    public void test(){
    //        System.out.println("定时任务执行！！！！！！！！！！！！");
    //    }


    //每天早上八点开启定时任务，通知orderService发送消息给就诊人
    @Scheduled(cron = "0 0 8 * * ?")
    public void task(){
        System.out.println(new Date().toLocaleString());
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_8, "");
    }

}

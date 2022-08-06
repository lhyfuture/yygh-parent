package com.atguigu.yygh.hosp.receiver;

import com.atguigu.yygh.common.service.MqConst;
import com.atguigu.yygh.common.service.RabbitService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Date;

@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private RabbitService rabbitService;



    //创建订单监听器，监听ORDER交换机的消息
    //监听到消息后 执行更新尚医通的排班信息 并使用rabbitService发送消息到msm交换机上
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER,durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}
    ))
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel)
            throws IOException {

        //1.取出参数
        String hoscode = orderMqVo.getHoscode();
        String hosScheduleId = orderMqVo.getScheduleId();
        Integer reservedNumber = orderMqVo.getReservedNumber();
        Integer availableNumber = orderMqVo.getAvailableNumber();

        //消息发送数据封装在了订单消息类中
        MsmVo msmVo = orderMqVo.getMsmVo();

        //2.根据参数查询排班信息
        Schedule schedule = scheduleService.getScheduleByIds(
                hoscode,hosScheduleId);

        //2.5判断是创建订单还是取消预约
        if(StringUtils.isEmpty(availableNumber)){
            availableNumber = schedule.getAvailableNumber().intValue() + 1;
            schedule.setAvailableNumber(availableNumber);
        }else {
            schedule.setReservedNumber(reservedNumber);
            schedule.setAvailableNumber(availableNumber);
        }
        //3更新排班信息

        schedule.setUpdateTime(new Date());
        scheduleService.update(schedule);

        //4发送短信相关MQ消息
        if(msmVo != null){
            rabbitService.sendMessage(
                    MqConst.EXCHANGE_DIRECT_MSM,MqConst.ROUTING_MSM_ITEM,msmVo);
        }

    }
}

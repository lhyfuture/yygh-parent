package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

//创建service
public interface OrderService extends IService<OrderInfo> {
    //创建订单
    Long submitOrder(String scheduleId, Long patientId);
    //订单列表（条件查询带分页）
    Page<OrderInfo> selectPage(Page<OrderInfo> pageParams, OrderQueryVo orderQueryVo);

    OrderInfo getOrderById(Long orderId);

    Boolean cancelOrder(Long orderId);

    /**
     * 就诊提醒
     */
    void patientTips();

    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);

}

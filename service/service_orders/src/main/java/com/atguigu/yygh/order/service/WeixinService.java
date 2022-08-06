package com.atguigu.yygh.order.service;

import java.util.Map;

public interface WeixinService {
    /**
     * 根据订单号下单，生成支付链接
     */
    Map createNative(Long orderId);

    Map<String, String> queryPayStatus(Long orderId, Integer paymentType);

    /***
     * 退款
     * @param orderId
     * @return
     */
    Boolean refund(Long orderId);


}
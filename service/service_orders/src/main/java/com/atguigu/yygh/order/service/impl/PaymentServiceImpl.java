package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.common.handler.GuliException;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.order.mapper.PaymentMapper;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class PaymentServiceImpl extends
        ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {

    @Autowired
    private OrderService orderService;
    /**
     * 保存交易记录
     * @param orderInfo
     * @param paymentType 支付类型（1：支付宝 2：微信）
     */

    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        //1.根据orderId+paymentType查询交易记录(payment_info表)
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",orderInfo.getId());
        wrapper.eq("payment_type",paymentType);
        //只需查询交易记录是否存在，不需要查出全部数据
        Integer count = baseMapper.selectCount(wrapper);
        if (count>0)return;
        //2.如果没有交易记录则添加新的交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        //订单交易号（订单创建时生成的唯一对外编号）
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        //到这一步一般状态为支付中，完成订单后改为支付完成
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        //交易内容  title医生职称
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")+"|"+orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle();
        paymentInfo.setSubject(subject);

        paymentInfo.setTotalAmount(orderInfo.getAmount());
        baseMapper.insert(paymentInfo);

    }

    //交易成功更新交易记录
    @Override
    public void paySuccess(String outTradeNo, Integer paymentType, Map<String, String> resultMap) {
        //1.查询交易记录，根据outTradeNo+paymentType
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",outTradeNo);
        wrapper.eq("payment_type",paymentType);
        PaymentInfo paymentInfo = baseMapper.selectOne(wrapper);
        if(paymentInfo==null){
            throw new GuliException(20001,"交易记录失效");
        }

        //2.判断交易记录状态（如果已支付直接返回不更新）
        if(paymentInfo.getPaymentStatus() == PaymentStatusEnum.PAID.getStatus()){
            return;
        }

        //3更新交易记录
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());//交易状态改为已支付
        paymentInfo.setTradeNo(resultMap.get("transaction_id"));//transaction_id微信方唯一订单编号
        paymentInfo.setCallbackTime(new Date());//回调时间
        paymentInfo.setCallbackContent(resultMap.toString());//将回调的全部内容转入数据库
        //baseMapper.updateById(paymentInfo);
        baseMapper.updateById(paymentInfo);

        //4.更新订单状态
        OrderInfo orderInfo = orderService.getById(paymentInfo.getOrderId());
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderService.updateById(orderInfo);
        //5调用医院接口更新订单状态（省略）
    }


    /**
     * 获取支付记录
     * @param orderId
     * @param paymentType
     * @return
     */
    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",orderId);
        wrapper.eq("payment_type",paymentType);
        PaymentInfo paymentInfo = baseMapper.selectOne(wrapper);
        return paymentInfo;
    }

}

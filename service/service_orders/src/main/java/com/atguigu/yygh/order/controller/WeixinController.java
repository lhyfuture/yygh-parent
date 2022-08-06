package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.R;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.WeixinService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/order/weixin")
public class WeixinController {
    @Autowired
    private WeixinService weixinPayService;
    @Autowired
    private PaymentService paymentService;

    /**
     * 下单 生成二维码
     */
    @ApiOperation(value = "生成二维码")
    @GetMapping("/createNative/{orderId}")
    public R createNative(
            @PathVariable("orderId") Long orderId) {
        Map<String,Object> map =
                weixinPayService.createNative(orderId);
        return R.ok().data(map);
    }


    @ApiOperation(value = "查询支付状态")
    @GetMapping("/queryPayStatus/{orderId}")
    public R queryPayStatus(
            @PathVariable("orderId") Long orderId){
        //1.根据参数查询交易状态
        Map<String, String> resultMap =
                weixinPayService.queryPayStatus(orderId, PaymentTypeEnum.WEIXIN.getStatus());
        //2.判断交易是否失败（resultMap==null）
        if(resultMap==null){
            return R.error().message("支付出错");
        }
        //3.判断交易成功
        if("SUCCESS".equals(resultMap.get("trade_state"))){
            //交易成功更新交易记录
            String outTradeNo = resultMap.get("out_trade_no");
            paymentService.paySuccess(outTradeNo, PaymentTypeEnum.WEIXIN.getStatus(),
                    resultMap);
            return R.ok().message("支付成功");
        }
        //4.还有可能正在支付中
        return R.ok().message("支付中");
    }
}

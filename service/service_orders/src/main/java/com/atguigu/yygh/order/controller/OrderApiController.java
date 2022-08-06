package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.R;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

//创建controller方法
@Api(tags = "订单接口")
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderApiController {

    @Autowired
    private OrderService orderService;


    @ApiOperation(value = "创建订单")
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public R submitOrder(@PathVariable String scheduleId,
                         @PathVariable Long patientId) {
        Long orderId = orderService.submitOrder(scheduleId,patientId);
        return R.ok().data("orderId",orderId);
    }

    //订单列表（条件查询带分页）
    @ApiOperation(value = "带条件带分页查询订单列表")
    @GetMapping("auth/{page}/{limit}")
    public R list(@PathVariable Long page,
                  @PathVariable Long limit,
                  OrderQueryVo orderQueryVo, HttpServletRequest request) {
        //1.获取userId，存入orderQueryVo（userId是在request请求中随token一起带过来的）
        Long userId = AuthContextHolder.getUserId(request);
        orderQueryVo.setUserId(userId);
        //2.封装分页参数
        Page<OrderInfo> pageParams = new Page<>(page,limit);
        //3.调用方法查询
        Page<OrderInfo> pageModel = orderService.selectPage(pageParams,orderQueryVo);
        return R.ok().data("pageModel",pageModel);
        
    }

    //获取订单状态集合，供前端下拉集合使用,枚举类中已经给出
    @ApiOperation(value = "获取订单状态")
    @GetMapping("auth/getStatusList")
    public R getStatusList(){
        return R.ok().data("statusList", OrderStatusEnum.getStatusList());
    }


    //根据订单id查询订单详情
    @ApiOperation(value = "根据订单id查询订单详情")
    @GetMapping("auth/getOrders/{orderId}")
    public R getOrders(@PathVariable Long orderId){
        OrderInfo orderInfo = orderService.getOrderById(orderId);
        return R.ok().data("orderInfo",orderInfo);
    }

    @ApiOperation(value = "取消预约")
    @GetMapping("auth/cancelOrder/{orderId}")
    public R cancelOrder(
            @PathVariable("orderId") Long orderId) {
        Boolean flag = orderService.cancelOrder(orderId);
        return R.ok().data("flag",flag);
    }



    @ApiOperation(value = "获取订单统计数据")
    @PostMapping("inner/getCountMap")
    public Map<String, Object> getCountMap(
            @RequestBody OrderCountQueryVo orderCountQueryVo) {
        //封装展示图表的x轴和y轴集合
        Map<String, Object> map =
                orderService.getCountMap(orderCountQueryVo);
        return map;
    }


}
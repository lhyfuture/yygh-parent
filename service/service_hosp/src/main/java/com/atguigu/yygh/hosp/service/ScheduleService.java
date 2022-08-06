package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    void saveSchedule(Map<String, Object> paramMap);

    Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    void remove(String hoscode, String hosScheduleId);

    Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode);

    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);

    //获取可预约排班数据
    Map<String, Object> getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode);

    Schedule findScheduleById(String id);

    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

     //修改排班
    void update(Schedule schedule);

    //根据医院编码、医院排班id获取排班信息
    Schedule getScheduleByIds(String hoscode,String hosScheduleId);


}
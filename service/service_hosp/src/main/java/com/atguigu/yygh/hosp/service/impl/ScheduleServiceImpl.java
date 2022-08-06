package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.handler.GuliException;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;


    @Override
    public void saveSchedule(Map<String, Object> paramMap) {
        //1转化参数类型
        String paramJsonString = JSONObject.toJSONString(paramMap);
        Schedule schedule = JSONObject.parseObject(paramJsonString,Schedule.class);

        //2.查询排版信息（hoscode，hosScheduleId）的json插入
        Schedule targetSchedule = scheduleRepository.getByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());

        if(targetSchedule!=null){
            //3存在，更新
            schedule.setId(targetSchedule.getId());
            schedule.setCreateTime(targetSchedule.getCreateTime());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }else{
            //4不存在，新增
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }
    }

    @Override
    public Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        //1.创建分页对象
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        Pageable pageable = PageRequest.of(page-1,limit,sort);
        //2.创建查询模板
        //查询对象是针对schedule，所以需要将scheduleQueryVo中的查询参数取出来看
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo,schedule);
        schedule.setIsDeleted(0);//?

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        
        //创建实例
        Example<Schedule> example = Example.of(schedule,matcher);
        //2.调用接口进行分页查询 Repository直接findAll就行
        Page<Schedule> pages = scheduleRepository.findAll(example, pageable);
        return pages;
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {

        //1.先根据传过来的参数查找到mongo数据的数据，如果不为空，调用scheduleRepository进行删除
        Schedule schedule = scheduleRepository.getByHoscodeAndHosScheduleId(hoscode, hosScheduleId);

        if(schedule!=null){
            //这里用的是schedule对象在mongo数据库中自身的id，不是医院传过来的唯一hosScheduleId
            scheduleRepository.deleteById(schedule.getId());
        }

    }


    @Override
    public Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode) {
        //1.创建返回对象
        Map<String, Object> result = new HashMap<>();
        //2.带条件带分页的聚合查询（分组加统计 返回list）
        //2.1创建查询条件对象Criteria
        Criteria criteria = Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode);
        //2.2创建聚合查询对象
        Aggregation agg = Aggregation.newAggregation(
                //2.2.1 设置查询条件
                Aggregation.match(criteria),
                Aggregation.group("workDate")
                .first("workDate").as("workDate")
                .count().as("docCount")
                .sum("reservedNumber").as("reservedNumber")
                .sum("availableNumber").as("availableNumber"),
                //2.2.3排序
                Aggregation.sort(Sort.Direction.ASC,"workDate"),
                //2.2.4分页
                Aggregation.skip((page-1)*limit),
                Aggregation.limit(limit)
        );
        //2.3进行聚合查询
        AggregationResults<BookingScheduleRuleVo> aggregate 
                = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();

        //3.再次进行带条件的聚合查询（返回total）
        //3.1创建聚合查询对象
        Aggregation aggTotal = Aggregation.newAggregation(
                //2.2.1设置查询条件
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        //3.2进行聚合查询
        AggregationResults<BookingScheduleRuleVo> aggregateTotal = 
                mongoTemplate.aggregate(aggTotal, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> totalList = aggregateTotal.getMappedResults();
        //3.3获取total(根据workDate分组后集合里面有多少条数据)
        int total = totalList.size();

        //4.遍历数据，使用工具，换算周几
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        //5.封装数据并返回
        result.put("bookingScheduleRuleList",bookingScheduleRuleVoList);
        result.put("total",total);

        //6.其他参数封装
        //6.1获取医院名称
        String hosName = hospitalService.getHospName(hoscode);
        //其他基础数据()
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname",hosName);
        result.put("baseMap",baseMap);
        return result;

    }



    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        //1查询排班数据
        //!!!java是强类型语言，查询一定要对应相应类型，实体类的workDate是Date类型
        List<Schedule> list =
                scheduleRepository.getByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDate());

        //2.翻译字段
        list.forEach(this::packageSchedule);

        return list;

    }


    //封装排班详情其他值 医院名称、科室名称、日期对应星期
    private Schedule packageSchedule(Schedule schedule) {
        //设置医院名称
        schedule.getParam().put("hosname",hospitalService.getHospName(schedule.getHoscode()));
        //设置科室名称
        schedule.getParam().put("depname",
                departmentService.getDepName(schedule.getHoscode(),schedule.getDepcode()));
        //设置日期对应星期
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
        return schedule;
    }




    //获取可预约排班数据
    @Override
    public Map<String, Object> getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String, Object> result = new HashMap<>();

        //1.根据hoscode查询医院信息，获取预约规则
        Hospital hospital = hospitalService.getHospital(hoscode);
        if (null == hospital){
            throw new GuliException(20001,"医院信息有误");
        }
        BookingRule bookingRule = hospital.getBookingRule();

        //2.根据预约规则，分页查询可预约的日期集合分页对象(IPage<Date>)
        //这里是对预约日期的计算封装，不适合mongo数据库，所以使用ipage分页更方便
        IPage<Date> ipage = this.getDateListPage(page,limit,bookingRule);
        List<Date> datePageList = ipage.getRecords();

        //3.参考后台接口实现聚合查询（List<BookingScheduleRuleVo>）
        //3.1准备筛选条件 增加workDate在日期集合datePageList中缩小范围，实体类也是Date属性
        //并将范围进一步缩小and("workDate").in(datePageList) 日期已经分页和按时间排序
        Criteria criteria = Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode).and("workDate").in(datePageList);
        //3.2创建聚合查询对象
        Aggregation agg = Aggregation.newAggregation(
                //3.2.1设置查询条件 有参构造
                Aggregation.match(criteria),
                //3.2.2设置聚合参数+聚合查询字段
                Aggregation.group("workDate")
                .first("workDate").as("workDate")
                .count().as("docCount")
                .sum("reservedNumber").as("reservedNumber")
                .sum("availableNumber").as("availableNumber")
        );
        //3.3执行聚合查询List<BookingScheduleRuleVo>
        AggregationResults<BookingScheduleRuleVo> aggregate = 
                mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleVoList =
                aggregate.getMappedResults();
        //3.4转化查询结果类型，List=>Map k:workDate v: BookingScheduleRuleVo
        Map<Date,BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(scheduleVoList)){
            scheduleVoMap = scheduleVoList.stream().collect(Collectors.toMap(
                    BookingScheduleRuleVo::getWorkDate,
                    BookingScheduleRuleVo-> BookingScheduleRuleVo
            ));
        }



        //4合并步骤2(datePageList)和步骤3(scheduleVoMap)数据
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();

        for (int i = 0,let = datePageList.size(); i < let; i++) {
            //4.1遍历datePageList，取出每一天日期
            Date date = datePageList.get(i);
            //4.2根据日期查询scheduleVoMap，获取排班聚合的记录信息
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            //4.3排班聚合的记录是空的，需要初始化(医院没有排班但是日期不能为空)
            if(bookingScheduleRuleVo == null){
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                bookingScheduleRuleVo.setDocCount(0);
                bookingScheduleRuleVo.setAvailableNumber(-1);

            }
            //4.4设置排班日期
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //4.5根据日期换算周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
            //4.6根据时间进行记录状态判断
            //状态 0：正常 1：即将放号 -1：当天已停止挂号
            //最后一页，最后一条记录，状态为 1：即将放号
            if(i==let-1&&page==ipage.getPages()){//第一页的最后一条可不是周期的最后一天！！！
                bookingScheduleRuleVo.setStatus(1);
            }else {
                bookingScheduleRuleVo.setStatus(0);
            }

            //第一页，第一条记录，如果已过停止挂号时间，状态为-1：当天已停止挂号
            if(i==0&&page==1){
                DateTime stopDateTime =
                        this.getDateTime(new Date(), bookingRule.getStopTime());
                if(stopDateTime.isBeforeNow()){
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }


        //5封装数据并返回
        //可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", ipage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        //科室
        Department department =departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;
    }

    private IPage<Date> getDateListPage(Integer page, Integer limit, BookingRule bookingRule) {
        //1.从预约规则中获取开始挂号的时间(当前系统时间（年月日）+开始时间（几点）)
        DateTime releaseDateTime =
                this.getDateTime(new Date(),bookingRule.getReleaseTime());
        //2.从预约规则中取出周期，判断周期是否需要+1
        Integer cycle = bookingRule.getCycle();
        if(releaseDateTime.isBeforeNow())cycle+=1;//开始挂号时间在现在具体时间之前
        //3.根据周期推算出可以挂号日期，存入集合list
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            DateTime plusDays = new DateTime().plusDays(i);
            String plusDaysString = plusDays.toString("yyyy-MM-dd");
            dateList.add(new DateTime(plusDaysString).toDate());
        }
        //4.因为周期一般大于七天，准备分页参数
        int start = (page-1)*limit;
        int end = (page-1)*limit+limit;
        //有可能预约周期小于分页长度，那么最后一天要修改
        if(end>dateList.size())end=dateList.size();
        //5.根据参数获取分页后日期集合
        List<Date> datePageList = new ArrayList<>();
        for (int i = start; i < end; i++) {//第一页从0开始，取不到end
            //从所以可以挂号日期中取出某一页日期集合
            datePageList.add(dateList.get(i));
        }
        //6.封装数据到IPage对象中返回 Page类data和baomodou重名
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page,limit,dateList.size());
        iPage.setRecords(datePageList);
        return iPage;
    }

    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString =
                new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;//拼个空格，日期和时间分开一下
        DateTime dateTime =
                DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }


    @Override
    public Schedule findScheduleById(String id) {
        Schedule schedule =
                this.packageSchedule(scheduleRepository.findById(id).get());
        return schedule;
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        //1.根据排班id获取排班信息
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        if(schedule==null){
            throw new GuliException(20001,"排班信息有误");
        }
        //2.根据排班信息获取hoscode并查询医院信息
        Hospital hospital = hospitalService.getHospital(schedule.getHoscode());
        if(hospital==null){
            throw new GuliException(20001,"医院信息有误");
        }
        //3.从医院信息中取出预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        if(null == bookingRule) {
            throw new GuliException(20001,"预约规则有误");
        }
        //4.封装基础数据
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        scheduleOrderVo.setHoscode(hospital.getHoscode());
        scheduleOrderVo.setHosname(hospital.getHosname());
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepartment(schedule.getHoscode(), schedule.getDepcode()).getDepname());
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //5.封装根据预约规则推算的时间信息
        //5.1推算可以退号的截止日期+时间
        //退号截止天数（如：就诊前一天为-1，当天为0）
        DateTime quitDate = new DateTime(schedule.getWorkDate()).plusDays(bookingRule.getQuitDay());
        //通过之前的方法拼日期+时间获取退号具体截止时间
        DateTime quitDateTime =
                this.getDateTime(quitDate.toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitDateTime.toDate());

        //5.2预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //5.3预约截止时间
        DateTime endTime = this.getDateTime(
                new DateTime().plusDays(bookingRule.getCycle()).toDate(),
                bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //5.4当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());
        return scheduleOrderVo;

    }


    @Override
    public void update(Schedule schedule) {
        scheduleRepository.save(schedule);
    }

    //根据医院方系统传过来的编码、医院排班id获取排班信息
    @Override
    public Schedule getScheduleByIds(String hoscode, String hosScheduleId) {
        Schedule schedule =
                scheduleRepository.getByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        return schedule;
    }
}
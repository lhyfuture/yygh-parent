package com.atguigu.yygh.cmn.test;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class Stu {
    //设置表头名称
    //@ExcelProperty("学生编号")读取表需要用索引指定表对应的列
    @ExcelProperty(value = "学生编号",index = 0)
    private int sno;
    //设置表头名称
    //@ExcelProperty("学生姓名")
    @ExcelProperty(value = "学生姓名",index = 1)
    private String sname;
}

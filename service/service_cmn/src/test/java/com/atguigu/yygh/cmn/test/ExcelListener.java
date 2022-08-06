package com.atguigu.yygh.cmn.test;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.Map;

public class ExcelListener extends AnalysisEventListener<Stu> {
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        System.out.println("headMap = " + headMap);
    }

    @Override
    public void invoke(Stu stu, AnalysisContext analysisContext) {
        System.out.println("stu = " + stu);


    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}

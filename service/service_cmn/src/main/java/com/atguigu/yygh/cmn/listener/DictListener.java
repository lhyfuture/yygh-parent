package com.atguigu.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellData;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DictListener extends AnalysisEventListener<DictEeVo> {
    @Autowired
    private DictMapper dictMapper;

    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        //读取的的DictEeVo数据存到数据库里面转换类型为Dict
        //因为mysql8数据表里面字段没有默认实现
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo,dict);
        dict.setIsDeleted(0);
        dictMapper.insert(dict);

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}

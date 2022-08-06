package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.handler.GuliException;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl
        extends ServiceImpl<DictMapper, Dict>
        implements DictService {

    @Autowired
    private DictListener dictListener;

    @Override
    public String getNameByInfo(String parentDictCode, String value) {
        if(StringUtils.isEmpty(parentDictCode)){
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("value", value));
            //可以在取name前进行非空校验
            if(dict!=null){
                return dict.getName();
            }
        }else {
            /*1.先根据parentDictCode在数据库中查到对应的唯一数据，
             this是controller方法调用接口是的实现类对象，因为后面会用到，抽取方法*/
            Dict parentDict = this.getDictByDictCode(parentDictCode);

            //2.根据查询到的父级对象的自身id找到子类集合，在通过value查询
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>()
                    .eq("parent_id", parentDict.getId())
                    .eq("value", value));

            //3.非空校验
            if(dict!=null){
                return dict.getName();
            }
        }
        return "";
    }

    //根据字典编码查询父级别数据
    private Dict getDictByDictCode(String parentDictCode){
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code",parentDictCode);
        Dict dict = baseMapper.selectOne(wrapper);
        return dict;
    }





    @Cacheable(value = "dict",key = "'selectIndexList' + #id")
    @Override
    public List<Dict> findChlidData(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        //根据id查询子级数据
        List<Dict> dictList  = baseMapper.selectList(wrapper);

        //遍历子级数据，查询是否有子节点hasChildren,以便让前端知道是否可以展开
        for (Dict dict : dictList) {
            //用每个dict对象的id去查是否有子数据，this代表什么？
            boolean hasChildren = this.isChildren(dict.getId());
            dict.setHasChildren(hasChildren);
        }
        return dictList;
    }

    //查询是否有子数据
    private boolean isChildren(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        //Integer count = baseMapper.selectCount(wrapper);
        Integer count = baseMapper.selectCount(wrapper);
        return count>0;
    }

    @Override
    public void exportData(HttpServletResponse response) {
        try {
            //1设置response的基本参数
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");

            //2.查询所有字典数据
            List<Dict> dictList = baseMapper.selectList(null);

            //3.因为读出的数据对象是DictEeVo类型，所有需要类型转换
            List<DictEeVo> dictEeVoList = new ArrayList<>();
            for (Dict dict : dictList) {
                DictEeVo dictEeVo = new DictEeVo();
                BeanUtils.copyProperties(dict,dictEeVo);
                dictEeVoList.add(dictEeVo);
            }

            //4.使用工具导出数据
            EasyExcel.write(response.getOutputStream(),DictEeVo.class)
                    .sheet("数据字典").doWrite(dictEeVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void importData(MultipartFile file) {//实现此方法前需要先创建监听器
        try {
            InputStream inputStream = file.getInputStream();
            EasyExcel.read(inputStream,DictEeVo.class,dictListener).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
            throw new GuliException(20001,"导入数据失败");
        }
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        Dict dictByDictCode = this.getDictByDictCode(dictCode);
        List<Dict> dictList = this.findChlidData(dictByDictCode.getId());
        return dictList;
    }
}

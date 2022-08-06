package com.atguigu.yygh.cmn.controller;


import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(description = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin
public class DictController {

    @Autowired
    private DictService dictService;

     /*（1）分析接口
     *参数parentId
     *返回值List<Dict>*/

     @ApiOperation(value = "根据数据id查询子数据列表")
     @GetMapping("findChildData/{id}")
     public R findChildData(@PathVariable("id") Long id){
         List<Dict> list =  dictService.findChlidData(id);
         return R.ok().data("list",list);

     }

    @ApiOperation(value = "导出字典数据")
    @GetMapping(value = "/exportData")
    public void exportData(HttpServletResponse response){
        dictService.exportData(response);
     }

    @ApiOperation(value = "导入字典数据")
    @PostMapping("importData")
    public R importData(MultipartFile file){
        dictService.importData(file);
        return R.ok();
    }


    @ApiOperation(value = "获取数据字典名称(自定义)")
    @GetMapping(value = "/getName/{parentDictCode}/{value}")
    public String getName(
            @PathVariable("parentDictCode") String parentDictCode,
            @PathVariable("value") String value){
        String name = dictService.getNameByInfo(parentDictCode,value);
        return name;
    }


    @ApiOperation(value = "获取数据字典名称（国标）")
    @GetMapping(value = "/getName/{value}")
    public String getName(
            @PathVariable("value") String value) {
        String name = dictService.getNameByInfo("",value);
        return name;
    }

    //实现根据字典编码查询子数据接口
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "/findByDictCode/{dictCode}")
    public R findByDictCode(
            @PathVariable String dictCode) {
        List<Dict> list =  dictService.findByDictCode(dictCode);
        return R.ok().data("list",list);
    }



}

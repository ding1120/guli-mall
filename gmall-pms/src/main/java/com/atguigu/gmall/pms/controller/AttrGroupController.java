package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 属性分组
 *
 * @author ding
 * @email ding@atguigu.com
 * @date 2020-12-15 15:58:31
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;


    @GetMapping("withattr/value/category/{cid}")
    public ResponseVo<List<ItemGroupVo>> queryGroupsWithAttrsAndValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid") Long cId,
            @RequestParam("skuId") Long skuId,
            @RequestParam("spuId") Long spuId
    ){
        List<ItemGroupVo> itemGroupVos=this.attrGroupService.queryGroupsWithAttrsAndValuesByCidAndSpuIdAndSkuId(cId,skuId,spuId);
        return ResponseVo.ok(itemGroupVos);
    }



    @GetMapping("withattrs/{categoryId}")
    public ResponseVo<List<AttrGroupEntity>> queryAttrsByCateId(@PathVariable Long categoryId){
       List<AttrGroupEntity> attrGroupEntities=attrGroupService.querybyCategoryId(categoryId);
        return ResponseVo.ok(attrGroupEntities);
    }

    /**
     * 查询规格分组(将几级分类进行查询)
     * 1.将商品根据其属性进行分类，之后将每一类商品中的包含的不同商品继续分类查询
     */
    @ApiOperation("根据三级分类进行查询")
    @GetMapping("category/{cid}")
    public ResponseVo<List<AttrGroupEntity>> queryGroupByCid(@PathVariable("cid")Long cid){
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id",cid);
        List<AttrGroupEntity> list = this.attrGroupService.list(queryWrapper);
        return ResponseVo.ok(list);
    }


    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrGroupByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = attrGroupService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrGroupEntity> queryAttrGroupById(@PathVariable("id") Long id){
		AttrGroupEntity attrGroup = attrGroupService.getById(id);

        return ResponseVo.ok(attrGroup);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		attrGroupService.removeByIds(ids);

        return ResponseVo.ok();
    }

}

package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;

import java.util.List;

/**
 * 属性分组
 *
 * @author ding
 * @email ding@atguigu.com
 * @date 2020-12-15 15:58:31
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    //    List<GroupVo> querybyCategoryId(Long categoryId);

   List<AttrGroupEntity> querybyCategoryId(Long categoryId);

    List<ItemGroupVo> queryGroupsWithAttrsAndValuesByCidAndSpuIdAndSkuId(Long cId, Long skuId, Long spuId);
}


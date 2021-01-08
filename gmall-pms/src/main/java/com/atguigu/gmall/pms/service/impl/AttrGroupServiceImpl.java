package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrGroupMapper attrGroupMapper;
    @Autowired
    AttrMapper attrMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SpuAttrValueMapper spuAttrValueMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrGroupEntity> querybyCategoryId(Long categoryId) {
        //根据分组id查询所有分组
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id",categoryId);
        List<AttrGroupEntity> attrGroupEntities = attrGroupMapper.selectList(queryWrapper);
        //遍历分组，查询每个组下的规格参数
        //1.先判断集合是否为空
        if(CollectionUtils.isEmpty(attrGroupEntities)){
           return null;
        }
        attrGroupEntities.forEach(t->{
            //录入的是spu信息，不需要录入sku信息，所以直接让type=1的遍历出来显示
            List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", t.getId()).eq("type", 1));

            t.setAttrEntities(attrEntities);
        });
        return attrGroupEntities;

    }

    @Override
    public List<ItemGroupVo> queryGroupsWithAttrsAndValuesByCidAndSpuIdAndSkuId(Long cId, Long skuId, Long spuId) {
       //1.根据分类id查询分组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cId));
        if(CollectionUtils.isEmpty(attrGroupEntities)){
            return null;
        }
        //2.遍历每一个分组查询组下规格参数
        List<ItemGroupVo> itemGroupVos = attrGroupEntities.stream().map(groupEntity -> {
            ItemGroupVo groupVo = new ItemGroupVo();
            groupVo.setId(groupEntity.getId());
            groupVo.setName(groupEntity.getName());

            // 查询组下的规格参数
            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", groupEntity.getId()));
            if (!CollectionUtils.isEmpty(attrEntities)) {
                // 获取规格参数id集合
                List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

                List<AttrValueVo> bigAttrValueVos = new ArrayList<>();
                // 3.查询销售属性的规格参数及值
                List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().in("attr_id", attrIds).eq("sku_id", skuId));
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                    bigAttrValueVos.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        AttrValueVo attrValueVo = new AttrValueVo();
                        BeanUtils.copyProperties(skuAttrValueEntity, attrValueVo);
                        return attrValueVo;
                    }).collect(Collectors.toList()));
                }

                // 4.查询基本属性的规格参数及值
                List<SpuAttrValueEntity> spuAttrValueEntities = this.spuAttrValueMapper.selectList(new QueryWrapper<SpuAttrValueEntity>().in("attr_id", attrIds).eq("spu_id", spuId));
                if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                    bigAttrValueVos.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        AttrValueVo attrValueVo = new AttrValueVo();
                        System.out.println("spuAttrValueEntity"+spuAttrValueEntity);
                        BeanUtils.copyProperties(spuAttrValueEntity, attrValueVo);
                        return attrValueVo;
                    }).collect(Collectors.toList()));
                }

                groupVo.setAttrs(bigAttrValueVos);
            }
            return groupVo;
        }).collect(Collectors.toList());
        System.out.println("itemGroupVos = " + itemGroupVos);
        return itemGroupVos;
    }


}
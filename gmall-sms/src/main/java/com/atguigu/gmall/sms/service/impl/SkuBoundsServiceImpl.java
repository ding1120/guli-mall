package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;

import com.atguigu.gmall.sms.vo.ItemSaleVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.atguigu.gmall.sms.vo.SkuSaleVo;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {
    @Autowired
    SkuBoundsMapper skuBoundsMapper;

    @Autowired
    SkuLadderMapper ladderMapper;
    @Autowired
    SkuFullReductionMapper reductionMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSkuSaleInfo(SkuSaleVo skuSaleVo) {
       // QueryWrapper<SkuSaleVo> queryWrapper = new QueryWrapper<>();
        //3.1 积分优惠
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleVo,skuBoundsEntity);
        //work优惠生效的情况
        List<Integer> work = skuSaleVo.getWork();
        if(!CollectionUtils.isEmpty(work)){
            //将0000-1111等情况转化成10进制
            skuBoundsEntity.setWork(work.get(0)*8+work.get(1)*4+work.get(2)*2+work.get(3));
        }
        skuBoundsMapper.insert(skuBoundsEntity);

        //3.2满减优惠
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleVo,skuFullReductionEntity);
        //其他优惠
        skuFullReductionEntity.setAddOther(skuSaleVo.getAddOther());
        this.reductionMapper.insert(skuFullReductionEntity);

        //3.3数量折扣
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleVo,skuLadderEntity);
        this.ladderMapper.insert(skuLadderEntity);
    }

    @Override
    public List<ItemSaleVo> querySalesBySkuId(Long skuId) {
        List<ItemSaleVo> saleVos=new ArrayList<>();
        //1.查询积分优惠信息
       SkuBoundsEntity skuBoundsEntity= this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
       if(skuBoundsEntity!=null){
           ItemSaleVo itemSaleVo = new ItemSaleVo();
           itemSaleVo.setType("积分");
            itemSaleVo.setDesc("送"+skuBoundsEntity.getGrowBounds()+"成长积分，送"+skuBoundsEntity.getBuyBounds()+"购物积分");

           saleVos.add(itemSaleVo);
       }
        //2.查询满减优惠信息
        SkuFullReductionEntity reductionEntity= reductionMapper.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if (reductionEntity != null) {
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setType("满减");
            itemSaleVo.setDesc("满" + reductionEntity.getFullPrice() + "减" + reductionEntity.getReducePrice());
            saleVos.add(itemSaleVo);
        }

        //3.查询打折优惠信息
        SkuLadderEntity skuLadderEntity=this.ladderMapper.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if (skuLadderEntity != null) {
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setType("打折");
            itemSaleVo.setDesc("满" + skuLadderEntity.getFullCount() + "件打" + skuLadderEntity.getDiscount().divide(new BigDecimal(10)) + "折");
            saleVos.add(itemSaleVo);
        }
        return saleVos;

    }

}
package com.atguigu.gmall.sms.service;


import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.vo.SkuSaleVo;

import java.util.List;

/**
 * 商品spu积分设置
 *
 * @author ding
 * @email ding@atguigu.com
 * @date 2020-12-15 18:23:28
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    void saveSkuSaleInfo(SkuSaleVo skuSaleVo);

    List<ItemSaleVo> querySalesBySkuId(Long skuId);
}


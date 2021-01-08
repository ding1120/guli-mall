package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 * 
 * @author ding
 * @email ding@atguigu.com
 * @date 2020-12-15 15:58:31
 */
@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValueEntity> {

    List<SkuAttrValueEntity> querySearchAttrValueBySkuId(Long skuId);

    //每一个map是一个对象，所以构成list集合
    List< Map<String,Object>> querySaleAttrValuesMappingSkuIdBySpuId(Long spuId);
}

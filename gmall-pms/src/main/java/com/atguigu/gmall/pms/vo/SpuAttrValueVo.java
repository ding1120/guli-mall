package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;


import java.util.List;

public class SpuAttrValueVo extends SpuAttrValueEntity {

    //将集合valueSelected作为参数传入方法中
    public void setValueSelected(List<Object> valueSelected){
        // 如果接受的集合为空，则不设置
        if(CollectionUtils.isEmpty(valueSelected)){
            return;
        }
        SpuAttrValueVo spuAttrValueVo = new SpuAttrValueVo();
        //将集合以逗号进行分割
        spuAttrValueVo.setAttrValue(StringUtils.join(valueSelected,","));
        //this.setAttrValue(StringUtils.join(valueSelected, ","));
    }
}

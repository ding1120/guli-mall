package com.atguigu.gmall.order.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.math.BigDecimal;

public class OrderItemVo {
    private Long skuId;
    private String defaultImage;
    private String title;
    private String saleAttrs; // 销售属性：List<SkuAttrValueEntity>的json格式
    private BigDecimal price; // 加入购物车时的价格
    private BigDecimal count;
    private Boolean store = false; // 是否有货
    private String sales; // 营销信息: List<ItemSaleVo>的json格式
    private Integer weight;
}

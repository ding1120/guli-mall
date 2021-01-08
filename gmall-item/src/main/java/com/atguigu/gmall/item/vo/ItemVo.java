package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ItemVo {
    /**
     * 基本属性
     */
    //一级，二级，三级分类(面包屑)
    private List<CategoryEntity> categories;
    //品牌
    private Long brandId;
    private String brandName;

    //spu
    private Long spuId;
    private String spuName;

    //sku
    private Long skuId;
    private String title;
    private String subtitle;
    private BigDecimal price;
    private String defaultImage;
    private Integer weight;

    //图片列表
    private List<SkuImagesEntity> skuImages;

    //营销信息
    private List<ItemSaleVo> sales;

    //是否有货
    private Boolean store;

    /**
     * 选择组合的属性
     */

    // sku所属spu下的所有sku的销售属性
    // [{attrId: 3, attrName: '颜色', attrValues: '白色','黑色','粉色'},
    // {attrId: 8, attrName: '内存', attrValues: '6G','8G','12G'},
    // {attrId: 9, attrName: '存储', attrValues: '128G','256G','512G'}]
    //销售的属性
    private List <SaleAttrValueVo> saleAttrs;

    // 当前sku的销售属性：{3:'白色',8:'8G',9:'128G'}
    private Map<Long,String> saleAttr;

    //销售属性组合和skuId的映射关系
    // sku列表：{'白色,8G,128G': 4, '白色,8G,256G': 5, '白色,8G,512G': 6, '白色,12G,128G': 7}
    private String skuJson;

    // spu的海报信息(商品详情地址)
    private List<String> spuImages;

    //规格参数
    private List<ItemGroupVo> groups;
}

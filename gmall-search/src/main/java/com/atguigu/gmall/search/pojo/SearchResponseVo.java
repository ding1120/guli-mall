package com.atguigu.gmall.search.pojo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

//封装一个对象，将对象响应给前端，显示成需要的页面
@Data
public class SearchResponseVo {
    //part1：查询条件(品牌，分类，规格参数)
    private List<BrandEntity> brands;
    private List<CategoryEntity> categories;

    // 规格参数过滤条件：[{attrId: 4, attrName: "内存", attrValues: ["8G", "12G"]},
    // {attrId: 5, attrName: "机身存储", attrValues: ["128G", "512G"]}]

    //需要过滤的规格参数
    private List<SearchResponseAttrVo> filters;

    //分页
    private Integer pageNum;
    private Integer pageSize;
    private Long total;

    //显示的数据模型
    private List<Goods> goodsList;
 }

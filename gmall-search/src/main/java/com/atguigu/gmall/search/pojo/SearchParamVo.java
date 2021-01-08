package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

//检索数据模型(用户查找商品时可搜索的属性)  仿照京东搜索的手机页面
@Data
public class SearchParamVo {
    //检索条件(关键字)
    private String keyword;
    //品牌id过滤
    private List<Long> brandId;
    //分类id过滤条件
    private List<Long> categoryId;
    //规格参数过滤条件：["4:8G-12G","5:128G-256G"]
    private List<String> props;

    //价格区间过滤
    private Double priceFrom;
    private Double priceTo;

    //是否有货过滤
    private Boolean store;

    // 排序：0-综合排序 1-价格降序 2-价格升序 3-销量的降序 4-新品降序
    private Integer sort;

    //分页参数：页码
    private Integer pageNum=1;
    //每页记录数
    private final Integer pageSize=20;


}

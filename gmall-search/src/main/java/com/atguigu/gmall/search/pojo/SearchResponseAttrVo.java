package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

//检索规格参数的数据模型
@Data
public class SearchResponseAttrVo {
    private Long attrId;
    private String attrName;
    private List<String> attrValues;

}

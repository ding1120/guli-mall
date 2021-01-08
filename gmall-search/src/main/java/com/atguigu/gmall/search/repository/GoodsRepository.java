package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

//进行基本的CRUD(文档的操作)
public interface GoodsRepository  extends ElasticsearchRepository<Goods,Long> {
}

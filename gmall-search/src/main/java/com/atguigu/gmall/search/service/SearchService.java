package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.BrandEntity;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * paramVo :是客户端查询时，服务端接收到的查询对象
 * responseVo :是服务端接收到查询请求之后的响应对象
 */
@Service
public class SearchService {

    //完成搜索，使用RestHighLevelClient客户端(进行信息的搜索)
    @Autowired
    RestHighLevelClient restHighLevelClient;

    public SearchResponseVo search(SearchParamVo paramVo) {
        try {
            //一、封装一个buildDsl()方法，构建SearchSourceBuilder(构建Dsl语句)
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDsl(paramVo));

            //options 请求就是预检请求
            SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //二、结果集解析_source
            SearchResponseVo responseVo=this.parseResult(response);
            //从查询条件中获取分页数据
            responseVo.setPageNum(paramVo.getPageNum());
            responseVo.setPageSize(paramVo.getPageSize());
            return responseVo;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 2.对结果集进行解析
     *  "hits" : {
     *     "total" : 14,
     *     "max_score" : 1.0,
     *     "hits" :{}
     *     }
     */
    private SearchResponseVo parseResult(SearchResponse response) {
        SearchResponseVo responseVo = new SearchResponseVo();
        
        //1.解析hits(即为页面中显示的sku详情)
        SearchHits hits = response.getHits();
        //2.总命中记录数(对响应给前端的对象，进行设置，将击中的记录数返回给前端显示的sku)
        responseVo.setTotal(hits.totalHits);
        //3.解析当前页的数据
        SearchHit[] hitsHits = hits.getHits();
        List<Goods> goodsList = Stream.of(hitsHits).map(hitsHit -> {
            String json = hitsHit.getSourceAsString(); //_source中的内容
            // 把_source反序列化为goods对象(将字符串反序列化成goods)
            Goods goods = JSON.parseObject(json, Goods.class);

            // 获取高亮结果集替换掉普通title
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("title");
            Text[] fragments = highlightField.getFragments();
            goods.setTitle(fragments[0].string());
            return goods;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);

        //4.解析聚合结果集，获取所有聚合，以map形式接受
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();
        //4.1 获取品牌id的聚合(解析成Long类型的词条)
        ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggregationMap.get("brandIdAgg");
        //获取桶
        List<? extends Terms.Bucket> brandBuckets= brandIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(brandBuckets)){
           responseVo.setBrands(brandBuckets.stream().map(bucket ->{
               BrandEntity brandEntity = new BrandEntity();
               brandEntity.setId(((Terms.Bucket)bucket).getKeyAsNumber().longValue());
               // 获取品牌id子聚合
               Map<String, Aggregation> subAggrregationMap = ((Terms.Bucket) bucket).getAggregations().asMap();
               // 解析品牌名称子聚合获取品牌名称
               ParsedStringTerms brandNameAgg=(ParsedStringTerms)subAggrregationMap.get("brandNameAgg");
               List<? extends Terms.Bucket> nameAggBuckets = brandNameAgg.getBuckets();
               if (!CollectionUtils.isEmpty(nameAggBuckets)){
                   brandEntity.setName(nameAggBuckets.get(0).getKeyAsString());
               }
               // 解析品牌logo子聚合获取品牌logo
               ParsedStringTerms logoAgg = (ParsedStringTerms)subAggrregationMap.get("logoAgg");
               List<? extends Terms.Bucket> logoBuckets = logoAgg.getBuckets();
               if(!CollectionUtils.isEmpty(logoBuckets)){
                   brandEntity.setLogo(logoBuckets.get(0).getKeyAsString());
               }
               return brandEntity;
                   }).collect(Collectors.toList()));
        }

        // 获取分类聚合
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryBuckets = categoryIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categoryBuckets)){
            responseVo.setCategories(categoryBuckets.stream().map(bucket->{
                CategoryEntity categoryEntity = new CategoryEntity();
                categoryEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                // 通过子聚合获取分类名称
                ParsedStringTerms categoryNameAgg = ((Terms.Bucket) bucket).getAggregations().get("categoryNameAgg");
                List<? extends Terms.Bucket> nameAggBuckets = categoryNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)){
                    categoryEntity.setName(nameAggBuckets.get(0).getKeyAsString());
                }
                    return categoryEntity;
            }).collect(Collectors.toList()));
        }


        // 获取规格参数聚合并解析出规格参数过滤列表
        ParsedNested attrAgg = (ParsedNested)aggregationMap.get("attrAgg");
        // 获取嵌套聚合中的子聚合，就是attrId的子聚合
        ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(attrIdAggBuckets)){
            responseVo.setFilters(attrIdAggBuckets.stream().map(bucket->{
                SearchResponseAttrVo responseAttrVo = new SearchResponseAttrVo();
                // 获取桶中的key，就是attrId
                responseAttrVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                // 获取attrIdAgg的子聚合：attrNameAgg attrValueAgg
                Map<String, Aggregation> subAggregationMap = ((Terms.Bucket) bucket).getAggregations().asMap();
                // 获取规格参数名称的子聚合，解析出规格参数名
                ParsedStringTerms attrNameAgg = (ParsedStringTerms) subAggregationMap.get("attrNameAgg");
                List<? extends Terms.Bucket> nameAggBuckets = attrNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)) {
                    responseAttrVo.setAttrName(nameAggBuckets.get(0).getKeyAsString());
                }

                // 获取规格参数值的子聚合，解析出规格参数的可选值
                ParsedStringTerms attrValueAgg = (ParsedStringTerms)subAggregationMap.get("attrValueAgg");
                List<? extends Terms.Bucket> buckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(buckets)){
                    responseAttrVo.setAttrValues(buckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList()));
                }
                return responseAttrVo;
            }).collect(Collectors.toList()));
        }
        return responseVo;
    }


    /**
     *1. 基于原生客户端构建查询条件
     * @param paramVo
     * @return
     */
    private SearchSourceBuilder buildDsl(SearchParamVo paramVo){
        //step1:创建SearchSourceBuilder的对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //判断检索条件类型是否为空(主查询输入框内)
        String keyword = paramVo.getKeyword();
        if(StringUtils.isBlank(keyword)){
            //TODO:打广告
            return sourceBuilder;
        }

        /**
         * 1.构建查询及过滤条件(布尔查询)
         */
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);

        //1.1构建匹配查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title",keyword).operator(Operator.AND));
        /**
         * 1.2 构建过滤条件
         */
        //1.2.1品牌过滤
        List<Long> brandId = paramVo.getBrandId();
        if(!CollectionUtils.isEmpty(brandId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",brandId));
        }
        //1.2.2分类过滤
        List<Long> categoryId = paramVo.getCategoryId();
        if(!CollectionUtils.isEmpty(categoryId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId",categoryId));
        }

        //1.2.3价格区间过滤
        Double priceFrom = paramVo.getPriceFrom();
        Double priceTo = paramVo.getPriceTo();
        if(priceFrom!=null||priceTo!=null){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            //再进行判断
            if(priceFrom!=null){
                rangeQuery.gte(priceFrom);
            }
            if (priceTo!=null){
                rangeQuery.lte(priceTo);
            }
            boolQueryBuilder.filter(rangeQuery);

        }
        //1.2.4是否有货
        Boolean store = paramVo.getStore();
        if(store!=null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("store",store));
        }
        //1.2.5规格参数过滤
        List<String> props = paramVo.getProps();
        if(!CollectionUtils.isEmpty(props)){
            props.forEach(prop->{//4:8G-12G&5:128G-512G
                //将遍历的规格参数以：进行分割
                String[] attr = StringUtils.split(prop, ":");
                if(attr!=null||attr.length==2) {
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    //规格参数单词条查询条件
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId", attr[0]));
                    //规格参数多词条查询条件
                    //将规格参数以-进行分割(将第二个参数进行分割1)
                    String[] attrValues = StringUtils.split(attr[1], "-");
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValues));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs", boolQuery, ScoreMode.None));

                }

            });
        }

        /**
         * 2.排序
         */

        Integer sort = paramVo.getSort();
        if(sort!=null){
            //排序分为四种情况
            switch (sort) {
                //价格高低排序
                case 1:sourceBuilder.sort("price", SortOrder.DESC);
                    break;
                case 2:sourceBuilder.sort("price", SortOrder.ASC);
                    break;
                //销量排序
                case 3:sourceBuilder.sort("sales", SortOrder.DESC);
                    break;
                //是否为新品
                case 4:sourceBuilder.sort("createTime", SortOrder.DESC);
                    break;
                //默认以价格降序排序
                default:
                    break;
            }
        }

        /**
         * 3.分页
         */
        Integer pageNum = paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        sourceBuilder.from((pageNum-1)*pageSize);
        sourceBuilder.size(pageSize);

        /**
         * 4.高亮
         */
        sourceBuilder.highlighter(
        new HighlightBuilder()
                .field("title")
                .preTags("<font style='color:red'>")
                .postTags("</font>")
        );

        /**
         * 5.聚合
         */

        //5.1 品牌聚合(嵌套聚合)
        sourceBuilder.aggregation(
                AggregationBuilders.terms("brandIdAgg").field("brandId")
                        .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                        .subAggregation(AggregationBuilders.terms("logoAgg").field("logo"))
        );

        //5.2 分类参数聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                        .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName"))
        );
        //5.3 规格参数嵌套聚合
        sourceBuilder.aggregation(
                AggregationBuilders.nested("attrAgg", "searchAttrs")
                        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue")))
        );

        /**
         * 6.结果集过滤
         */
        sourceBuilder.fetchSource(new String[]{"skuId", "defaultImage", "title", "subTitle", "price"}, null);
        System.out.println(sourceBuilder);
        return sourceBuilder;


    }
}

package com.atguigu.es.demo;

import com.atguigu.es.demo.pojo.User;
import com.atguigu.es.demo.repository.UserRepository;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class EsDemoApplicationTests {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RestHighLevelClient restHighLevelClient;

    // ElasticsearchTemplate是TransportClient客户端
    // ElasticsearchRestTemplate是RestHighLevel客户端
    @Autowired
    ElasticsearchRestTemplate restTemplate;

    @Test
    void contextLoads() {
        // 创建索引
        this.restTemplate.createIndex(User.class);
        // 创建映射
        this.restTemplate.putMapping(User.class);
        // 删除索引
        // this.restTemplate.deleteIndex("user");
    }

    @Test
    void testAdd(){
        userRepository.save(new User(1L,"钟硕",20,"123456"));
    }

    @Test
    void testFind(){
        System.out.println(this.userRepository.findById(1L).get());
        this.userRepository.findAll().forEach(System.out::println);
    }

    @Test
    void testAddAll(){
        List<User> users = new ArrayList<>();
        users.add(new User(1l, "柳冰冰", 18, "123456"));
        users.add(new User(2l, "范冰冰", 19, "654321"));
        users.add(new User(3l, "李冰冰", 20, "654321"));
        users.add(new User(4l, "锋哥", 21, "123456"));
        users.add(new User(5l, "小冰冰", 22, "654321"));
        users.add(new User(6l, "韩冰冰", 23, "123456"));
        this.userRepository.saveAll(users);
    }

    //在指定区间进行查询
    @Test
    void testFindAgeBetWeen(){
        System.out.println(this.userRepository.findByAgeBetween(20,30));
    }

    @Test
    void testFindByQuery(){
        System.out.println(this.userRepository.findByQuery(20,30));
    }

    @Test
    void testFind1(){
        this.userRepository.findByName("冰冰").forEach(System.out::println);
    }

    //基于querybuilders查询自定义查询

    @Test
    void testSearch(){
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.rangeQuery("age").gte(20).lte(22));
        boolQuery.must(QueryBuilders.matchQuery("name","冰冰"));        // this.userRepository.search(QueryBuilders.matchQuery("name","冰冰")).forEach(System.out::println);
        this.userRepository.search(boolQuery).forEach(System.out::println);
        //this.userRepository.search(QueryBuilders.rangeQuery("age").lte(23).gte(21)).forEach(System.out::println);

    }
    //基于NativeSearchQueryBuilder自定义复杂查询
    @Test
    void testNative(){
        //初始化定义查询对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //构建查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("name", "冰冰"));
        //查询
        queryBuilder.withSort(SortBuilders.fieldSort("age").order(SortOrder.DESC));
        //分页
        queryBuilder.withPageable(PageRequest.of(0,2));
        //高亮,不能获取，所以用用原生的客户端进行获取
       //queryBuilder.withHighlightBuilder(new HighlightBuilder().field("name").preTags("<em>").postTags("</em>"));
       //过滤
        queryBuilder.withFilter(QueryBuilders.rangeQuery("age").gte(18).lte(23));
       //过滤结果集
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"name","age"},null));
        queryBuilder.addAggregation(AggregationBuilders.terms("pwdAgg").field("password"));
        AggregatedPage<User> page = (AggregatedPage<User>)this.userRepository.search(queryBuilder.build());
        page.getContent().forEach(System.out::println);
        ParsedStringTerms pwdAgg=(ParsedStringTerms)page.getAggregation("pwdAgg");
        List<? extends Terms.Bucket> buckets = pwdAgg.getBuckets();
        buckets.forEach(bucket->{
            System.out.println(bucket.getKeyAsString());
        });
        //this.userRepository.search(queryBuilder.build()).forEach(System.out::println);

    }

    @Test
    void testRestHignLevelClient() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        SearchRequest user = searchRequest.indices("user");//搜索的索引库
        //检索条件构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("nmae","冰冰"));
        System.out.println(searchSourceBuilder);
        //设置查询条件
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(search);
    }
}

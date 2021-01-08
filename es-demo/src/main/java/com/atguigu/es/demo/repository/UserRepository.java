package com.atguigu.es.demo.repository;

import com.atguigu.es.demo.pojo.User;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

//ElasticSearch是最为强大的接口功能
public interface UserRepository  extends ElasticsearchRepository<User,Long> {
    /**
     * 根据年龄区间查询
     */
    List<User> findByAgeBetween(Integer age1,Integer age2);

    //使用注解方式
    @Query("{\n" +
            "    \"range\": {\n" +
            "      \"age\": {\n" +
            "        \"gte\": \"?0\",\n" +
            "        \"lte\": \"?1\"\n" +
            "      }\n" +
            "    }\n" +
            "  }")
    List<User> findByQuery(Integer from,Integer to);

    @Query(" {\n" +
            "    \"match\": {\n" +
            "      \"name\": \"冰冰\"\n" +
            "    }\n" +
            "    }")
    List<User> findByName(String name);
}


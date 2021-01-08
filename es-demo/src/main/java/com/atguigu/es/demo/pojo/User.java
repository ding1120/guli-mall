package com.atguigu.es.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
//对应的索引库,标记实体类为文档对象
@Document(indexName = "user",type = "info",shards = 3,replicas = 2)
public class User {
    @Id //声明为主键 对应_id
    private Long id;
    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private  String name;
    @Field(type=FieldType.Integer)
    private  Integer age;
    @Field(type = FieldType.Keyword)
    private String password;
}
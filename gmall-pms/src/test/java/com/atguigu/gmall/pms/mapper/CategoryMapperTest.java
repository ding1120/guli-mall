package com.atguigu.gmall.pms.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;




class CategoryMapperTest {
    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    void queryCategoriesWithSubsByPid(){
        System.out.println(this.categoryMapper.queryCategoriesWithSubsByPid(1L));
    }
}
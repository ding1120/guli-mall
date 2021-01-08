package com.atguigu.gmall.pms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {
    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCatgoriesByPid(Long parentId) {
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        //先判断是否有parentId,如果为-1，代表用户没有传该字段，则查询一级分类所有
        if(parentId!=-1){
            wrapper.eq("parent_id",parentId);
        }
        return categoryMapper.selectList(wrapper);

    }

    @Override
    public List<CategoryEntity> queryCategoriesWithSubsByPid(Long pid) {
       return this.categoryMapper.queryCategoriesWithSubsByPid(pid);
    }

    @Override
    public List<CategoryEntity> queryLv123CategoriesByCid3(Long id) {
        //查询3级分类
        CategoryEntity lvl3Category = this.getById(id);
        if(lvl3Category==null){
            return null;
        }
        CategoryEntity lvl2Category = this.getById(lvl3Category.getParentId());
        CategoryEntity lvl1Category = this.getById(lvl2Category.getParentId());

        return Arrays.asList(lvl1Category,lvl2Category,lvl3Category);
    }

}
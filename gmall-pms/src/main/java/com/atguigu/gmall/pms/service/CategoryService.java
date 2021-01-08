package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author ding
 * @email ding@atguigu.com
 * @date 2020-12-15 15:58:31
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<CategoryEntity> queryCatgoriesByPid(Long pid);

    List<CategoryEntity> queryCategoriesWithSubsByPid(Long pid);

    List<CategoryEntity> queryLv123CategoriesByCid3(Long id);
}


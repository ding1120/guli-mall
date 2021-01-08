package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CommentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价
 * 
 * @author ding
 * @email ding@atguigu.com
 * @date 2020-12-15 15:58:31
 */
@Mapper
public interface CommentMapper extends BaseMapper<CommentEntity> {
	
}

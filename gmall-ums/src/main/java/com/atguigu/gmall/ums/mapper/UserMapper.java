package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 * 
 * @author ding
 * @email ding@atguigu.com
 * @date 2020-12-15 17:50:15
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	
}

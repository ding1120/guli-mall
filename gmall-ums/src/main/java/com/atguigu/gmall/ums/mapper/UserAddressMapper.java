package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货地址表
 * 
 * @author ding
 * @email ding@atguigu.com
 * @date 2020-12-15 17:50:15
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddressEntity> {
	
}
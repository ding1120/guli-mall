package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author ding
 * @email ding@atguigu.com
 * @date 2020-12-15 18:44:30
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
	
}

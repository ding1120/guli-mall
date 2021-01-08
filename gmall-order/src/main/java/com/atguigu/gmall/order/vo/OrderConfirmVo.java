package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmVo {
    //收货人列表
    private List<UserAddressEntity> addresses;

    //送货清单
    private List<OrderItemVo> items;

    //购物积分
    private Integer bounds;

    //为防止订单多次提交，给当前页面订单生成一个唯一标识token
    private String orderToken;
}

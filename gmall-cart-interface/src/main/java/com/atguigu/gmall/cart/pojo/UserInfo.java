package com.atguigu.gmall.cart.pojo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserInfo {
    private Long userId;
    private String userKey;//游客的唯一标识
}

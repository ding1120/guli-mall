package com.atguigu.gmall.order.service;

import com.atguigu.gmall.order.feign.GmallPmsClient;
import com.atguigu.gmall.order.feign.GmallSmsClient;
import com.atguigu.gmall.order.feign.GmallUmsClient;
import com.atguigu.gmall.order.feign.GmallWmsClient;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    GmallPmsClient pmsClient;

    @Autowired
    GmallSmsClient smsClient;

    @Autowired
    GmallUmsClient umsClient;

    @Autowired
    GmallWmsClient wmsClient;
    public OrderConfirmVo confirm() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        confirmVo.setAddresses(null);
        confirmVo.setItems(null);
        confirmVo.setBounds(null);
        confirmVo.setOrderToken(null);
        return null;
    }
}

package com.atguigu.gmall.scheduled.jobhandler;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.scheduled.mapper.CartMapper;
import com.atguigu.gmall.scheduled.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyJobHandler {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    CartMapper cartMapper;

    private static final String KEY_PREFIX = "cart:info:";
    private static final String EXCEPTION_KEY="cart:exception:userId";
    //声明该方法是一个的定时任务(myJobHandler唯一标识)
    @XxlJob("myJobHandler")
    public ReturnT<String> test(String param){
        System.out.println("任务执行开始时间："+System.currentTimeMillis()+param);
        XxlJobLogger.log("MyJobHandler executed"+param);
        return ReturnT.SUCCESS;
    }

    @XxlJob("cartDataSyncJobHandler")
    public ReturnT<String> dataSync(String param){
        //读取redis中失败的用户信息
        BoundSetOperations<String, String> setOps = this.stringRedisTemplate.boundSetOps(EXCEPTION_KEY);
        //随机获取并移除一个用户
        String userId = setOps.pop();
        while(userId!=null){
            //全部删除失败用户的mysql中的购物车
            this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId));

            //读取redis中的失败用户的所有的购物车记录,如果redis中没有用户的购物车
            if(!this.stringRedisTemplate.hasKey(KEY_PREFIX+userId)){
                return ReturnT.SUCCESS;
            }
            BoundHashOperations<String, Object, Object> hashOps = this.stringRedisTemplate.boundHashOps(KEY_PREFIX + userId);
            List<Object> cartJsons = hashOps.values();
            cartJsons.forEach(cartJson->{
                //新增redis中对应的购物车记录
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                this.cartMapper.insert(cart);
            });

            userId=setOps.pop();
        }
        return ReturnT.SUCCESS;
    }
}

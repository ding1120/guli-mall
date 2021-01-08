package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.aspect.GmallCache;
import com.atguigu.gmall.index.config.RedissonConfig;


import com.atguigu.gmall.index.feign.GmallPmsFeign;
import com.atguigu.gmall.index.tools.DistributedLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//分布式锁的意义就是减少远程调用，保护好数据库
@Service
public class IndexService {
    @Autowired
    GmallPmsFeign pmsFeign;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    DistributedLock distributedLock;
    @Autowired
    RedissonClient redissonClient;


    //用：分隔,在Redis客户端中也会将文件夹分类(设置key的前缀,区分自己所添加数据)
    private static final String KEY_PREFIX="index:cates";


    public List<CategoryEntity> queryLvl1Categories() {
        ResponseVo<List<CategoryEntity>> responseVo = this.pmsFeign.queryCatgoriesByPid(0L);
        return responseVo.getData();
    }

    @GmallCache(prefix = KEY_PREFIX, timeout = 43200, random = 4320, lock = "index:lock")
    public List<CategoryEntity> queryLvl2CategoriesWithSubByPid(Long pid) {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsFeign.queryCategoriesWithSubsByPid(pid);
        List<CategoryEntity> data = listResponseVo.getData();
        return data;
    }

    public List<CategoryEntity> queryLvl2CategoriesWithSubByPid2(Long pid) {
        //1.先查询缓存(存时以key进行存储，当取出也要以他进行查询)
        String json=this.redisTemplate.opsForValue().get(KEY_PREFIX+pid);
        //json不能为空，同时json不等于null字符串
        if (StringUtils.isBlank(json)&&!StringUtils.equals("null",json)){
            //将反序列化的json返回
            return JSON.parseArray(json, CategoryEntity.class);
        }else if(StringUtils.equals("null", json)){
            return null;
        }

        //解觉缓存击穿(只锁一个)
        RLock lock = this.redissonClient.getLock("index:lock:" + pid);
        lock.lock();

        //在加锁时，避免其他数据获取到锁，将数据放入缓存中，所以再次判断是否有缓存
        String json2=this.redisTemplate.opsForValue().get(KEY_PREFIX+pid);
        //jso不能为空，同时json不等于null字符串
        if (StringUtils.isBlank(json2)&&!StringUtils.equals("null",json2)){
            //将反序列化的json返回
            return JSON.parseArray(json2, CategoryEntity.class);
        }else if(StringUtils.equals("null", json2)){
            return null;
        }

        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsFeign.queryCategoriesWithSubsByPid(pid);
        List<CategoryEntity> data = listResponseVo.getData();

        //放入缓存(避免击穿，null也缓存)
            if(CollectionUtils.isEmpty(data)){
                //如果数据为null缓存3分钟
                this.redisTemplate.opsForValue().set(KEY_PREFIX+pid,null,3, TimeUnit.MINUTES);

            }else{
                //为了防止缓存雪崩，给缓存时间添加随机值
                this.redisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(data),30+new Random().nextInt(10), TimeUnit.DAYS);

            }

        return data;
    }

    public  void testLock2() {
        //防止误删锁
        String uuid= UUID.randomUUID().toString();
        Boolean flag = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid,3,TimeUnit.SECONDS);
        if (!flag) {
            try {
                Thread.sleep(100);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {

            //this.redisTemplate.expire("lock", 3, TimeUnit.SECONDS);
            //查询redis中的num值
            String number = this.redisTemplate.opsForValue().get("number");
            //没有该值return
            if (StringUtils.isBlank(number)) {
                return;
            }
            //有值就转为int
            int num = Integer.parseInt(number);
            redisTemplate.opsForValue().set("number", String.valueOf(++num));

            //释放锁

            String script="if(redis.call('get',KEYS[1])==ARGV[1])then return redis.call('del','lock') end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList("lock") ,uuid);
//            if(StringUtils.equals(uuid, this.redisTemplate.opsForValue().get("lock"))){
//                this.redisTemplate.delete("lock");
//            }
        }
    }

    //可重入锁优化(枷锁)
    public void testLock3(){
        String uuid = UUID.randomUUID().toString();
        Boolean flag = this.distributedLock.tryLock("lockName", uuid, 30);
        if(flag) {
            String number = this.redisTemplate.opsForValue().get("number");
            //没有该值return
            if (StringUtils.isBlank(number)) {
                return;
            }
            //有值就转为int
            int num = Integer.parseInt(number);
            redisTemplate.opsForValue().set("number", String.valueOf(++num));

            //this.testSubLock(uuid);
            try {
                TimeUnit.SECONDS.sleep(90);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            this.distributedLock.unlock("lock", uuid);
        }
    }

    public void testSubLock(String uuid){
        this.distributedLock.tryLock("lock", uuid, 30);
        System.out.println("测试可重入锁");
        this.distributedLock.unlock("lock", uuid);
    }

    public void testLock(){
        //获取锁
        RLock lock = this.redissonClient.getLock("lock");
        lock.lock();
        String number = this.redisTemplate.opsForValue().get("number");
        //没有该值return
        if (StringUtils.isBlank(number)) {
            return;
        }
        //有值就转为int
        int num = Integer.parseInt(number);
        redisTemplate.opsForValue().set("number", String.valueOf(++num));

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lock.unlock();

    }

    public void testRead() {
        RReadWriteLock rwLock = this.redissonClient.getReadWriteLock("rwLock");
        rwLock.readLock().lock(10, TimeUnit.SECONDS);
        System.out.println("=================");
    }

    public void testWrite() {
        RReadWriteLock rwLock = this.redissonClient.getReadWriteLock("rwLock");
        rwLock.writeLock().lock(10, TimeUnit.SECONDS);
        System.out.println("===============================");
    }

    public void testLatch() {
        RCountDownLatch latch = this.redissonClient.getCountDownLatch("latch");
        latch.trySetCount(6);

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testCountdown() {
        RCountDownLatch latch = this.redissonClient.getCountDownLatch("latch");

        latch.countDown();
    }
}

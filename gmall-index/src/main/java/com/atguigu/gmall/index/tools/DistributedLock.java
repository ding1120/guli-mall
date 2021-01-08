package com.atguigu.gmall.index.tools;


import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class DistributedLock {

    @Autowired
    StringRedisTemplate redisTemplate;

    private Timer timer;

    public Boolean tryLock(String lockName,String uuid,Integer expire){
        String script= "if (redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1) " +
                "then" +
                "    redis.call('hincrby', KEYS[1], ARGV[1], 1);" +
                "    redis.call('expire', KEYS[1], ARGV[2]);" +
                "    return 1;" +
                "else" +
                "   return 0;" +
                "end";
        if(this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class),
                Arrays.asList(lockName),uuid,expire.toString())){

            //没有获取到锁就重试
            try {
                Thread.sleep(200);
                tryLock(lockName, uuid, expire);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            renewExpire(lockName, uuid, expire);
        }

        //获取到锁，返回true
        return true;
    }

    public void unlock(String lockName,String uuid){
        String script="if (redis.call('hexists', KEYS[1], ARGV[1]) == 0) then" +
                "    return nil;" +
                "end;" +
                "if (redis.call('hincrby', KEYS[1], ARGV[1], -1) > 0) then" +
                "    return 0;" +
                "else" +
                "    redis.call('del', KEYS[1]);" +
                "    return 1;" +
                "end;";
        // 这里之所以没有跟加锁一样使用 Boolean ,这是因为解锁 lua 脚本中，三个返回值含义如下：
        // 1 代表解锁成功，锁被释放
        // 0 代表可重入次数被减 1
        // null 代表其他线程尝试解锁，解锁失败
        Long result = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Lists.newArrayList(lockName), uuid);
        // 如果未返回值，代表尝试解其他线程的锁
        if(result==null){
            throw new RuntimeException("你在尝试解除别人的锁或者该锁不存在");
        }else if(result==1){
            timer.cancel();
        }
    }

    //锁延期方法：开启子线程执行延期
    /**
     * 锁延期
     * 线程等待超时时间的2/3时间后,执行锁延时代码,直到业务逻辑执行完毕,因此在此过程中,其他线程无法获取到锁,保证了线程安全性
     * @param lockName
     * @param expire 单位：毫秒
     */
    public void renewExpire(String lockName,String uuid ,Integer expire){
       String script="if redis.call('exists', KEYS[1]) == 1 then return redis.call('expire', KEYS[1], ARGV[1]) else return 0 end";

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList(lockName), uuid,expire.toString());
            }
        },expire*1000/3,expire*1000/3);


    }

    public static void main(String[] args) {
     /* 1.   System.out.println(System.currentTimeMillis());
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(()->{
            System.out.println(System.currentTimeMillis());
        }, 10, 10, TimeUnit.SECONDS);*/

     /*2. new Thread(()->{
         while(true){
             try {
                 TimeUnit.SECONDS.sleep(10);
                 System.out.println(System.currentTimeMillis());
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     },"").start();
    }*/

    /* 3.定时器
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(System.currentTimeMillis());
            }
        },10000,10000)*/
    }
}

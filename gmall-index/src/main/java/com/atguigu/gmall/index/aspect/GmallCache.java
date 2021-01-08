package com.atguigu.gmall.index.aspect;

import java.lang.annotation.*;

//ElementType.TYPE 可以作用在类上  ElementType.METHOD可以作用在放法上
@Target({ ElementType.METHOD})
//代表什么类型注解，是运行时还是编译时
@Retention(RetentionPolicy.RUNTIME)
//是否可以继承
@Inherited
//需不需要加到文档类
@Documented
public @interface GmallCache {


    /**
     * 缓存key的前缀
     * key:prefix+":"+方法参数
     * @return
     */
    String prefix() default "";

    /**
     * 指定缓存时间
     * 单位是分钟
     * 默认是5分钟
     * @return
     */
    int timeout() default 5;

    /**
     * 为了防止缓存雪崩
     * 让注解开发人员指定时间的随机值范围
     * 默认是5分钟
     * @return
     */
    int random() default 5;

    /**
     * 为了防止缓存击穿
     * 让注解开发人员指定分布式key
     * 默认是lock
     * key:lock+":"+pid
     * @return
     */
    String lock() default "lock";
}

package com.atguigu.gmall.order.interceptor;

import com.atguigu.gmall.cart.pojo.UserInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//将拦截器中获取到的登录信息传递给后端的业务逻辑
@Component
public class LoginInterceptor implements HandlerInterceptor {
    //声明一个现程的局部变量ThreadLocal(设为私有，不是谁都能获取)
    private static final ThreadLocal<UserInfo> THREAD_LOCAL=new ThreadLocal<UserInfo>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfo userInfo = new UserInfo();
        //从头信息中获取userId
        Long userId = Long.valueOf(request.getHeader("userId"));


        userInfo.setUserId(userId);

        //将信息放入线程的局部变量
        THREAD_LOCAL.set(userInfo);

        // 目的统一获取登录状态，不管有没有登录都要放行
        return true; //放行
    }

    //将threadlocal获取到的信息传递给后续的业务逻辑
    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    //在视图渲染结束后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 这里一定记得要手动清理threadlocal中的线程局部变量，因为使用的是tomcat线程池，请求结束线程没有结束，否则容易产生内存泄漏
        THREAD_LOCAL.remove();
    }
}

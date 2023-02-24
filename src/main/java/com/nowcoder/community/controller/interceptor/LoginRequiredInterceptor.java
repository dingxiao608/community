package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired LoginRequired = method.getAnnotation(LoginRequired.class);
            //如果请求映射的方法有@LoginRequired注解，并且当前没有登录（即hostHolder中没有用户），那么就重定向到登录页面，并且不继续执行请求映射的方法（return false）
            if (LoginRequired != null && hostHolder.getUser() == null){
                response.sendRedirect( request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}

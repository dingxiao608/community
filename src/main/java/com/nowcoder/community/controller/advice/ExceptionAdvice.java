package com.nowcoder.community.controller.advice;

import com.nowcoder.community.utils.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)//表示只扫描带有@Controller注解的那些组件
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    // @ExceptionHandler用于修饰方法，该方法会在Controller出现异常后被调用，用于处理捕获到的异常。
    // 该注解的括号里可以写要处理哪些注解，下面是直接使用了Exception.class，它是所有异常的父类，表示当前方法捕获所有异常并处理
    // @ExceptionHandler修饰的方法有很多参数，常用的有3个，如下：
    // Exception e：表示当Controller发生异常，会被下面的方法捕获，并且将异常封装到参数e中
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletResponse response, HttpServletRequest request) throws IOException {
        //// 1、记录日志。
        logger.error("服务器发生异常：" + e.getMessage());
        // 遍历异常的栈的信息，每个element记录了一个异常的相关信息
        for (StackTraceElement element : e.getStackTrace()){
            logger.error(element.toString());
        }

        // 2、记录完日志后，需要给浏览器响应。
        // 判断当前是普通请求还是异步请求。因为普通请求发生异常时要返回错误页面，异步请求发生异常时要返回json字符串
        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)){
            response.setContentType("application/plain;charset=utf-8");//"application/plain"表示向浏览器返回一个普通字符串，浏览器自身会通过$.parseJSON()转化为js对象
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常！"));
        }else {
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }

}

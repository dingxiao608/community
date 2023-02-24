package com.nowcoder.community.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect//表示这是一个切面（方面）组件
public class AlphaAspect {

    // @Pointcut 定义切点。这里我们定义为所有的service组件的方法
    // 第一个*表示方法返回值可以是任意类型。com.nowcoder.community.service.*.*(..)表示service包下的所有service组件（第二个*）的所有方法（第三个*表示方法，第三个*后面的(..)表示方法参数任意）
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){

    }

    @Before("pointcut()")//切点前面织入（要执行）的代码
    public void before(){
        System.out.println("before");
    }

    @After("pointcut()")//切点后面织入的代码
    public void after(){
        System.out.println("after");
    }

    @AfterReturning("pointcut()")//方法返回值后织入的代码
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    @AfterThrowing("pointcut()")//抛出异常后执行的代码
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }

    @Around("pointcut()")//切点前后都织入
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("around before");//执行之前
        Object obj = joinPoint.proceed();// 调用目标组件的方法（被织入代码的那个方法）
        System.out.println("around after");//执行之后
        return obj;
    }
}

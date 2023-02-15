package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
//@Scope("prototype") //加上这个注解，那么每次applicationContext.getBean都会重新实例化一个新的AlphaService组件，一般不用
public class AlphaService {

    public AlphaService() {
        System.out.println("实例化AlphaService");
    }

    @PostConstruct//在构造器之后执行
    public void init(){
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("销毁AlphaService");
    }

    @Autowired
    private AlphaDao alphaDao;

    public String find(){
        return alphaDao.select();
    }
}

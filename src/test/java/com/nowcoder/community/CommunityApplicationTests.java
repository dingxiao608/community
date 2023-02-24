package com.nowcoder.community;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.AlphaDaoMybatisImp;
import com.nowcoder.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//指定配置类为CommunityApplication.class
class CommunityApplicationTests implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;//这里获得的applicationContext就是IOC容器
    }

    //1.测试从IOC容器中获取Bean
    @Test
    public void testApplicationContext(){
        System.out.println(applicationContext);//GenericWebApplicationContext
        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
        System.out.println(alphaDao.select());//Hibernate

        AlphaDao alphaDaoMybatisImp = applicationContext.getBean("alphaDaoMybatisImp", AlphaDaoMybatisImp.class);
        System.out.println(alphaDaoMybatisImp.select());//Mybatis
    }

    //2.测试Bean管理
    @Test
    public void testBeanManagement(){
        AlphaService alphaService = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService);
    }

    //3.测试第三方bean
    @Test
    public void testSimpleDateFormat(){
        SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);
        System.out.println(simpleDateFormat.format(new Date()));//2023-02-13 17:46:43
    }

    //4.测试依赖注入
    @Autowired//自动注入
    @Qualifier("alphaDaoMybatisImp")//AlphaDaoHibernateImp组件设置了优先级@Primary，但是通过@Qualifier可以指定注入哪个组件
    private AlphaDao alphaDao;

    @Autowired
    private AlphaDaoMybatisImp alphaDaoMybatisImp;

    @Test
    public void testAutowired(){
        System.out.println(alphaDao);//AlphaDaoMybatisImp
        System.out.println(alphaDaoMybatisImp);//AlphaDaoMybatisImp
    }

}

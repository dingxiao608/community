package com.nowcoder.community.dao;


import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary//表示该组件优先
public class AlphaDaoHibernateImp implements AlphaDao{
    @Override
    public String select() {
        return "Hibernate";
    }
}

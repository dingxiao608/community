package com.nowcoder.community.dao;

import org.springframework.stereotype.Repository;

@Repository//默认组件名称为 首字母小写的类名，可以 @Repository("组件名") 为组件起名
public class AlphaDaoMybatisImp implements AlphaDao{
    @Override
    public String select() {
        return "Mybatis";
    }
}

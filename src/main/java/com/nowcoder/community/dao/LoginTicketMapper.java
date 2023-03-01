package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket(user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")   //useGeneratedKeys = true开启主键自增，keyProperty = "id"将自增后的主键值封装回loginTicket对象的id属性
    int insertLoginTicket(LoginTicket loginTicket);


    @Select({
            "select id, user_id, ticket, status, expired from login_ticket ",
            "where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
            "<script> ",
                "update login_ticket set status=#{status} where ticket=#{ticket} ",
                    "<if test=\"ticket!=null\"> ",
                            "and 1=1 ",
                    "</if> ",
            "</script>"
    })
    int updateStatus(String ticket, int status);
}

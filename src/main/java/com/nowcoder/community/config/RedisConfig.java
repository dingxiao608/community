package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    // 因为要连接redis数据库，那么需要一个连接工厂，这里通过参数底层直接封装
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 配置template主要配置序列化方式，因为程序是java程序，得到的数据是java类型的数据，最终要把这些数据存到redis数据库里，就要指定一种序列化的方式（即数据转化的方式）
        // 设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());// RedisSerializer.string()返回一个能够序列化字符串的序列化器
        // 设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json()); // value可以是普通的值、集合、列表，通常将其序列化为json
        // 设置hash的key的序列化方式（因为hash本身就是value，但是这个value又有key-value，所以需要单独设置）
        template.setHashKeySerializer(RedisSerializer.string());
        // 设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        // 使template里面的设置生效
        template.afterPropertiesSet();

        return template;
    }
}

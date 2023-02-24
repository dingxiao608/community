package com.nowcoder.community.utils;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    // 获取对某个实体赞的key（实体可以是帖子or评论）
    // key的样式为"like:entity:entityType:entityId"，value为userId（方便统计哪些用户给这个entity点赞了，用set存储）
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
}

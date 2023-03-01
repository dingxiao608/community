package com.nowcoder.community.utils;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";

    private static final String PREFIX_KAPTCHA = "kaptcha";

    private static final String PREFIX_TICKET = "ticket";

    private static final String PREFIX_USER = "user";



    // 获取对某个实体赞的key（实体可以是帖子or评论）
    // key的样式为"like:entity:entityType:entityId"，value为userId（方便统计哪些用户给这个entity点赞了，用set存储）
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 获取用户获得的赞 的key
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 获取某个用户关注的实体 key（举例：某个用户关注的帖子，存储的是）
    // followee:userId:entityType -> zset(entityId, now)   用zset，存入关注实体的id和关注时间，这样方便后面根据时间来排序
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 获取某个用户拥有的粉丝 的key（存储的是粉丝的id和关注时间）
    // follower:entityType:entityId -> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 生成验证码的key(owner是一个零时的key，指定该验证码属于哪个用户)
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 生成登录凭证的key
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 生成缓存用户信息的key
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }
}

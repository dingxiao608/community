package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowerService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    // 关注
    public void follow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    // 取关
    public void unfollow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec();
            }
        });
    }

    // 查询关注的实体的数量
    public long findFolloweeCount(int userId, int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    // 查询实体的粉丝数量
    public long findFollowerCount(int entityId, int entityType){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    // 查询当前用户是否关注了某实体
    public boolean hasFollowed(int userId, int entityType, int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    // 查询某个用户关注的人
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit){
        // 查询关注的用户的id
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Integer> followeesIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if (followeesIds == null){
            return null;
        }

        List<Map<String, Object>> followees = new ArrayList<>();
        // 查询关注的用户和关注时间
        for(Integer followeeId : followeesIds){
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(followeeId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, followeeId);
            map.put("followTime", new Date(score.longValue()));
            followees.add(map);
        }
        return followees;
    }

    // 查询某个用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit){
        // 查询粉丝的id
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> followersIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if (followersIds == null){
            return null;
        }

        List<Map<String, Object>> followers = new ArrayList<>();
        // 查询关注的用户和关注时间
        for(Integer followerId : followersIds){
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(followerId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, followerId);
            map.put("followTime", new Date(score.longValue()));
            followers.add(map);
        }
        return followers;
    }
}

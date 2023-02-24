package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString(){
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey, 1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));       // 1
        System.out.println(redisTemplate.opsForValue().increment(redisKey)); // 2
        System.out.println(redisTemplate.opsForValue().decrement(redisKey)); // 1
    }

    @Test
    public void testHash(){
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "Tom");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));   //1
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));   //Tom
    }

    @Test
    public void testList(){
        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().size(redisKey)); // 3
        System.out.println(redisTemplate.opsForList().index(redisKey, 0)); // 103
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));// 103 102 101

        System.out.println(redisTemplate.opsForList().leftPop(redisKey)); // 103
    }

    @Test
    public void testSet(){
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey, "张云", 11, "张飞");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testSortedSet(){
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey, "唐僧", 80);
        redisTemplate.opsForZSet().add(redisKey, "悟空", 90);
        redisTemplate.opsForZSet().add(redisKey, "八戒", 70);
        redisTemplate.opsForZSet().add(redisKey, "沙僧", 60);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey)); // 4
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "八戒")); // 70
        System.out.println(redisTemplate.opsForZSet().rank(redisKey, "八戒")); // 1（从低到高排序，返回八戒的名次，0开始）
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2)); // 悟空 唐僧 八戒（从高到低排序，返回前三名）
    }

    @Test
    public void testKeys(){
        redisTemplate.delete("test:user");

        System.out.println(redisTemplate.hasKey("test:user"));  // false

        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);//设置 test:students 的过期时间为10s
    }

    // 多次访问同一个key时，通过绑定，可以不需要每次都输入key
    @Test
    public void testBoundOperation(){
        String redisKey = "test:count";
        BoundValueOperations boundValueOps = redisTemplate.boundValueOps(redisKey);
        //后面的操作直接调用 boundValueOps，方法都与 redisTemplate 相同，但是不需要传入key"test:count"
        boundValueOps.set(1);
        boundValueOps.increment();
        System.out.println(boundValueOps.get()); // 2

        redisKey = "test:students";
        BoundSetOperations boundSetOperations = redisTemplate.boundSetOps(redisKey);
        boundSetOperations.add("张飞", "赵云");
        System.out.println(boundSetOperations.members()); // 张飞 赵云
    }

    // 编程式事务
    @Test
    public void testTransactional(){
        // new SessionCallback()的execute方法会将执行命令的对象（redisTemplate）传入到参数operations
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";

                operations.multi(); // 开启事务

                operations.opsForSet().add(redisKey, "tom");
                operations.opsForSet().add(redisKey, "mary");
                operations.opsForSet().add(redisKey, "lucy");

                System.out.println(operations.opsForSet().members(redisKey)); // []，什么都查不到，因为没有提交事务，所以尽量不要在redis事务中使用查询操作

                return operations.exec(); //提交事务
            }
        });

        System.out.println(obj); // [1, 1, 1, [lucy, tom, mary]]，前3个"1"表示上面3条add指令执行后影响的行数
    }
}

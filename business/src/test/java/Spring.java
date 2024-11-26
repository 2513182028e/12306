import com.java.train.business.BusinessApplication;
import com.java.train.business.entity.Train;
import com.java.train.business.service.Impl.KafkaUtilsServiceImple;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest(classes = {BusinessApplication.class})
public class Spring {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private KafkaUtilsServiceImple kafkaUtilsServiceImple;

    @Resource
    private RedissonClient redissonClient;
    @Test
    public void tt() throws InterruptedException {
        Train train = new Train();
        train.setId(123L);
        train.setCode("2131");
        redisTemplate.opsForValue().set("k3","v3");
//        kafkaUtilsServiceImple.send("tickets","1",JSON.toJSONString(train));
//        redisTemplate.opsForValue().set("k2","v2");
//        stringRedisTemplate.opsForValue().set("k2","v2");
//        RLock lock = redissonClient.getLock("fff");
//        for (int i = 0; i < 3; i++) {
//            boolean tried = lock.tryLock(2, 10, TimeUnit.SECONDS);
//            if (tried) {
//                System.out.println("抢到锁了");
//                System.out.println("锁的次数加一后" + lock.getHoldCount());
//            }
//            lock.unlock();
//        }
//        for (int i = 0; i < 3; i++) {
//            try {
//                boolean tried = lock.tryLock(0, 10, TimeUnit.SECONDS);
//                if (tried) {
//                    System.out.println("抢到锁了");
//                    Train train = new Train();
//                    train.setId(12314L);
//                    System.out.println("锁的次数加一后" + lock.getHoldCount());
//                    train.setCode("124");
//                    redisTemplate.opsForValue().set("train2", train);
//                    Train o = (Train) redisTemplate.opsForValue().get("train2");
//
//
//                    System.out.println(o);
//                }
//            } catch (Exception e) {
//                System.out.println("sd");
//            } finally {
//                if (null != lock && lock.isHeldByCurrentThread()) {
//                    lock.unlock();
//                    System.out.println("释放锁后的持有人数" + lock.getHoldCount());
//                }
//            }
//        }
    }
}

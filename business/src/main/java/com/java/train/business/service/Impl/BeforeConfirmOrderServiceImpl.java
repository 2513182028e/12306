package com.java.train.business.service.Impl;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.java.train.business.Dto.ConfirmOrderMQDto;
import com.java.train.business.entity.ConfirmOrder;
import com.java.train.business.enums.CacheEnum;
import com.java.train.business.enums.ConfirmOrderStatusEnum;
import com.java.train.business.mapper.ConfirmOrderMapper;
import com.java.train.business.req.ConfirmOrderDoReq;
import com.java.train.business.req.ConfirmOrderTicketReq;
import com.java.train.common.context.LoginMemberContext;
import com.java.train.common.exception.BusinessException;
import com.java.train.common.exception.BusinessExceptionEnum;
import com.java.train.common.util.ShowUtil;
import jakarta.annotation.Resource;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class BeforeConfirmOrderServiceImpl {

    @Resource
    private Sk_tokenServiceImpl skTokenService;

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private KafkaTemplate<String,String> kafkaTemplate;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
//    @Resource
//    private RedissonClient redissonClient;
    private final static String topic="tickets";

    private final static  String scripts= "-- 参数:\n" +
            "-- KEYS[i]: 每一个票种的库存 key (日期 + 车次编号 + 座位类型)\n" +
            "-- KEYS[i+N]: 每一个票种的秒杀标记集合 key (日期 + 车次编号)\n" +
            "-- ARGV[i]: 对应的用户标识或请求唯一标识符 (乘客 ID 或请求唯一标识符)\n" +
            "\n" +
            "local success = true  -- 标记所有操作是否成功\n" +
            "local failureKeys = {}  -- 用于存储扣减失败的票种\n" +
            "\n" +
            "-- 遍历所有票种\n" +
            "for i = 1, #KEYS/2  do\n" +
            "    local key = KEYS[i]  -- 票种库存的 key\n" +
            "    local set_key = KEYS[i+#KEYS/2 ]  -- 秒杀标记的 key\n" +
            "    local userKey = ARGV[i]  -- 乘客 ID 或请求唯一标识符\n" +
            "\n" +
            "    -- 检查该用户是否已经参与过秒杀\n" +
            "    if redis.call('SISMEMBER', set_key, userKey) == 1 then\n" +
            "        -- 标记为失败，记录失败的票种\n" +
            "        table.insert(failureKeys, key)\n" +
            "        success = false\n" +
            "    else\n" +
            "        -- 获取剩余库存\n" +
            "        local currentSeats = tonumber(redis.call('GET', key))\n" +
            "        if currentSeats and currentSeats > 0 then\n" +
            "            -- 库存足够，扣减库存\n" +
            "            redis.call('DECR', key)\n" +
            "\n" +
            "            -- 将用户标识加入集合中，标记该用户已进行秒杀\n" +
            "            redis.call('SADD', set_key, userKey)\n" +
            "\n" +
            "            -- 设置该用户标记的过期时间，防止用户长时间占用标记\n" +
            "            redis.call('EXPIRE', set_key, 60)  -- 假设过期时间为 60 秒\n" +
            "        else\n" +
            "            -- 如果库存不足，则标记为失败\n" +
            "            table.insert(failureKeys, key)\n" +
            "            success = false\n" +
            "        end\n" +
            "    end\n" +
            "end\n" +
            "\n" +
            "-- 如果有任何失败的操作，回退已执行的操作\n" +
            "if not success then\n" +
            "    -- 回退：恢复所有已扣减的库存\n" +
            "    for _, key in ipairs(failureKeys) do\n" +
            "        redis.call('INCR', key)  -- 恢复库存\n" +
            "        redis.call('SREM', set_key, ARGV[_])  -- 移除用户标记\n" +
            "    end\n" +
            "    return 0  -- 秒杀失败\n" +
            "end\n" +
            "\n" +
            "return 1  -- 秒杀成功";

    private  final  static String pre_topic="pre_tickets";
    private  static  List<String > keys;

    private  static  List<String > Id_keys;


        private  static  final Logger LOG= LoggerFactory.getLogger(BeforeConfirmOrderServiceImpl.class);
        public  Long  doBeforeComfirmOrder(ConfirmOrderDoReq req) throws InterruptedException {

            req.setMemberId(LoginMemberContext.getId());
        boolean validated = skTokenService.ValidateSKToken(req.getDate(), req.getTrainCode(), LoginMemberContext.getId());
        if(validated)
        {   LOG.info("令牌校验通过");

        }
        else {
            LOG.info("令牌校验不通过");
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SK_TOKEN_FAIL);
        }

            List<ConfirmOrderTicketReq> tickets = req.getTickets();
            //利用redis来扣减库存
            //String key= CacheEnum.Ticket_Nums.getCode()+":"+;
           for (int i=0;i<tickets.size();i++)
           {
               ConfirmOrderTicketReq ticketReq = tickets.get(i);
               String key=CacheEnum.Ticket_Nums.getCode()+":"+req.getDate()+"-"+req.getTrainCode()+"-"+ticketReq.getSeatTypeCode();
               String set_key=CacheEnum.Ticket_Nums.getCode()+":"+req.getDate()+"-"+req.getTrainCode();
               String id=ticketReq.getPassengerId().toString();
               keys.add(key);
               keys.add(set_key);
               Id_keys.add(id);
           }
            Long executed = redisTemplate.execute(new DefaultRedisScript<>(scripts, Long.class), keys, Id_keys);
            if(executed==0L)
            {
                LOG.info("库存不足");
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_TicketNumbers_Error);
            }
            keys.clear();

            Id_keys.clear();
            //生成订单
            DateTime now = DateTime.now();
            ConfirmOrder confirmOrder = new ConfirmOrder();
            confirmOrder.setId(ShowUtil.getSnowflakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrder.setMemberId(req.getMemberId());
            confirmOrder.setDate(req.getDate());
            confirmOrder.setTrainCode(req.getTrainCode());
            confirmOrder.setStart(req.getStart());
            confirmOrder.setEnd(req.getEnd());
            confirmOrder.setDailyTrainTicketId(req.getDailyTrainTicketId());
            confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
            confirmOrder.setTickets(JSON.toJSONString(req.getTickets()));
            kafkaTemplate.send(pre_topic,"confirmOrder",JSON.toJSONString(confirmOrder));
            //confirmOrderMapper.insert(confirmOrder);
//            ConfirmOrderMQDto orderMQDto = new ConfirmOrderMQDto();
//            orderMQDto.setDate(req.getDate());
//            orderMQDto.setTrainCode(req.getTrainCode());
//            orderMQDto.setLogId(MDC.get("LOG_ID"));
//            String reqJson = JSON.toJSONString(orderMQDto);
//            LOG.info("排队购票，发送mq开始，消息：{}",reqJson);
//            kafkaTemplate.send(topic,"req",reqJson);
            LOG.info("排队购票成功，发送mq结束");
            return confirmOrder.getId();
        }
}

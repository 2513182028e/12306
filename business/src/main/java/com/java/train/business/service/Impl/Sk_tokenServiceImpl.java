package com.java.train.business.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.java.train.business.controller.ConfirmOrderController;
import com.java.train.business.entity.SkToken;
import com.java.train.business.enums.LockKeyPreEnum;
import com.java.train.business.mapper.SkTokenMapperCust;
import com.java.train.common.exception.BusinessException;
import com.java.train.common.exception.BusinessExceptionEnum;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class Sk_tokenServiceImpl {



    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private SkTokenMapperCust skTokenMapperCust;

    private  static final Logger LOG= LoggerFactory.getLogger(Sk_tokenServiceImpl.class);




    boolean ValidateSKToken(Date date,String trainCode,Long memberId)
    {
        String lockKey= LockKeyPreEnum.STREAM_LOCK.getCode()+"-"+DateUtil.formatDate(date)+"-"+trainCode+"-"+memberId;
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey, 5, TimeUnit.SECONDS);
        if(Boolean.TRUE.equals(aBoolean))
        {
            LOG.info("恭喜，抢到了令牌锁！lockKey：{}",lockKey);
        }else
        {
            LOG.info("很遗憾，没有抢到了令牌锁！lockKey：{}",lockKey);
            return  false;
        }
        String skTokenCountKey=LockKeyPreEnum.SK_TOKEN_COUNT.getCode()+"-"+DateUtil.formatDate(date)+"-"+trainCode;
        Object skTokenCount = redisTemplate.opsForValue().get(skTokenCountKey);
        if(skTokenCount!=null)
        {
            LOG.info("缓存中有该车次的大令牌的key：{}",skTokenCountKey);
            Long counted = redisTemplate.opsForValue().decrement(skTokenCountKey, 1);
            redisTemplate.expire(skTokenCountKey,60,TimeUnit.SECONDS);
            if(counted<0)
            {
                LOG.error("获取令牌失败：{}",skTokenCountKey);
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SK_TOKEN_FAIL);
            }
            else{
                LOG.info("获取令牌后，令牌余数：{}",counted);
                if(counted%5==0)
                {
                    skTokenMapperCust.decrease(date,trainCode,5);
                    return  true;
                }
            }
        }
            LOG.info("缓存中没有该车次的大令牌的key：{}",skTokenCountKey);
            QueryWrapper<SkToken> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("date",date)
                    .eq("train_code",trainCode);
            List<SkToken> skTokenList = skTokenMapperCust.selectList(queryWrapper);
            if(CollUtil.isEmpty(skTokenList))
            {
                LOG.info("找不到日期为:{}车次【{}】的令牌信息",DateUtil.formatDate(date),trainCode);
                return  false;
            }
            else{
                SkToken skToken = skTokenList.get(0);
                Integer count=skToken.getCount()-1;
                skToken.setCount(count);
                redisTemplate.opsForValue().set(skTokenCountKey, String.valueOf(count),60,TimeUnit.SECONDS);
                return  true;
            }


    }
}

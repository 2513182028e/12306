package com.java.train.business.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import jakarta.annotation.Resource;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;
import java.util.Date;

@Configuration
public class BloomFilterConfig {

    @Resource
    private RedissonClient redissonClient;


    final static  int expectedNum=1000;
    final  static Double FPP=0.00001;

    @Bean(value = "bloomFilter")
    public RBloomFilter<String> bloomFilter()
    {

//        BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.forName("utf-8")), expectedNum, FPP);
//        return bloomFilter;

        RBloomFilter<String> bloomFilter1 = redissonClient.getBloomFilter("user");
        bloomFilter1.tryInit(expectedNum,FPP);
        return  bloomFilter1;
    }

}

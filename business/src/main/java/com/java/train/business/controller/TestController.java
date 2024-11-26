package com.java.train.business.controller;


import com.java.train.business.config.TopicUtils;
import com.java.train.business.entity.Station;
import com.java.train.common.util.ShowUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/topic")
public class TestController {

    @GetMapping("/list")
    public String topicList() throws ExecutionException, InterruptedException {
        TopicUtils topicUtils = new TopicUtils();

        //topicUtils.createTopic(name,num,rep);
        topicUtils.ListTopic();
        //topicUtils.DescribeTopic(name);
        return "success";
    }
    @GetMapping("/find/{TopicName}")
    public String topicDescs(@PathVariable("TopicName") String name) throws ExecutionException, InterruptedException {
        TopicUtils topicUtils = new TopicUtils();

        //topicUtils.createTopic(name,num,rep);
        topicUtils.DescribeTopic(name);
        //topicUtils.DescribeTopic(name);

        return "success";
    }
    @GetMapping("/createTopic/{TopicName}/{num}/{rep}")
    public String topicTest(@PathVariable("TopicName") String name,@PathVariable("num") Integer num,
                            @PathVariable("rep")Short rep) throws ExecutionException, InterruptedException {
        TopicUtils topicUtils = new TopicUtils();

        //topicUtils.createTopic(name,num,rep);
        topicUtils.ListTopic();
        //topicUtils.DescribeTopic(name);

        return "success";
    }



//    @Resource
//    private RedisTemplate<String,Object> redisTemplate;
//
//    @GetMapping("/hello")
//    public  String hellow()
//    {
//        //redisTemplate.opsForValue().set("HL","mama");
//        Station station = new Station();
//        station.setId(ShowUtil.getSnowflakeNextId());
//        station.setName("火车东站");
//        redisTemplate.opsForValue().set("HMM",station);
//        Station hmm = (Station)redisTemplate.opsForValue().get("HMM");
//        return hmm.toString();
//    }
}

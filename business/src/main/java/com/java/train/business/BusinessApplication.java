package com.java.train.business;


import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;


@SpringBootApplication
@EnableFeignClients("com.java.train.business.feign")
@EnableDiscoveryClient
@EnableKafka
public class BusinessApplication {

    private static  final Logger Log= LoggerFactory.getLogger(BusinessApplication.class);

//    @Resource
//    private RedisTemplate<String,Object> redisTemplate;

    public static void main(String[] args) {
        //SpringApplication.run(MemberApplication.class, args);
        SpringApplication   app=new SpringApplication(BusinessApplication.class);
        Environment env=app.run(args).getEnvironment();
        Log.info("启动成功");
        Log.info("地址:\t http://127.0.0.1:{}{}/hello",env.getProperty("server.port"),env.getProperty("server.servlet.context-path"));
        //initFlowRules();
    }

    private static  void  initFlowRules()
    {
        List<FlowRule> rules=new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setRefResource("doConfirm");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(1);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

}

package com.java.train.business.config;

import com.java.train.business.BusinessApplication;
import org.apache.kafka.clients.admin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class TopicUtils {

    private static  final Logger Log= LoggerFactory.getLogger(TopicUtils.class);
    public  static  String BOOTSTRAP_SERVER="192.168.43.137:9092";

    public   AdminClient createClient()
    {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,BOOTSTRAP_SERVER);
        return  AdminClient.create(properties);
    }

    public void createTopic(String TopicName,int numbPartition,Short replication)
    {
        AdminClient client = createClient();
        //创建topic，指定分区数为2，副本数为1
        NewTopic topic = new NewTopic(TopicName, numbPartition, replication);

        CreateTopicsResult result = client.createTopics(List.of(topic));

        try {
            result.all().get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        Log.info("创建的新的topic:"+TopicName);
    }

    public void deleteTopic(String TopicName)
    {
        AdminClient client = createClient();


        DeleteTopicsResult result = client.deleteTopics(Collections.singletonList(TopicName));

        try {
            result.all().get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        Log.info("删除了topic:"+TopicName);
    }
    public void ListTopic() throws ExecutionException, InterruptedException {
        AdminClient client = createClient();


        ListTopicsResult listTopicsResult = client.listTopics();

        Set<String> set = listTopicsResult.names().get();
        for (String name:set)
        {
            Log.info("Topic列表中的话题依次为:"+name);
        }


    }

    public void DescribeTopic(String TopicName) throws ExecutionException, InterruptedException {
        AdminClient client = createClient();


        DescribeTopicsResult result = client.describeTopics(Collections.singletonList(TopicName));
        Map<String, TopicDescription> descriptionMap = result.all().get();
        Set<Map.Entry<String, TopicDescription>> entrySet = descriptionMap.entrySet();

        entrySet.forEach(entry -> Log.info("Topic:"+entry.getKey()+"的具体的信息为"+entry.getValue()));

    }



}

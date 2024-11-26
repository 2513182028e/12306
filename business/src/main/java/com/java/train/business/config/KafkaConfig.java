//package com.java.train.business.config;
//
//
//import cn.hutool.log.Log;
//import jakarta.annotation.Resource;
//import org.apache.kafka.clients.admin.AdminClient;
//import org.apache.kafka.clients.admin.AdminClientConfig;
//import org.apache.kafka.clients.admin.CreateTopicsResult;
//import org.apache.kafka.clients.admin.NewTopic;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//import java.util.concurrent.ExecutionException;
//
//@Configuration
//public class KafkaConfig {
//
//
//    @Value("${spring.kafka.bootstrap-servers}")
//    private static String BOOTSTRAP_SERVER;
//
//    @Value("${spring.kafka.producer.key-serializer}")
//    private String ProducerKeySerial;
//
//    @Value("${spring.kafka.producer.value-serializer}")
//    private String ProducerValueSerial;
//
//    @Value("${spring.kafka.consumer.key-deserializer}")
//    private String ConsumerKeySerial;
//
//    @Value("${spring.kafka.consumer.value-deserializer}")
//    private String ConsumerValueSerial;
//
//
//    @Value("${spring.kafka.consumer.enable-auto-commit}")
//    private boolean ENABLE_AUTO_COMMIT_CONFIG;
//
//    @Bean("kafkaTemplated")
//    public KafkaTemplate<String,String> kafkaTemplate()
//    {
//        HashMap<String, Object> properties = new HashMap<>();
//        // 基本设置
//        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,BOOTSTRAP_SERVER);
//        //生产者设置
//        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,ProducerKeySerial);
//        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,ProducerValueSerial);
//
//        //消费者设置
//        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,ConsumerKeySerial);
//        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,ConsumerValueSerial);
//        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,ENABLE_AUTO_COMMIT_CONFIG);
//        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(properties));
//    }
//
//
//
//
//
//
//
//
//
//
//}

package com.java.train.business.service.Impl;

import cn.hutool.log.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalPacket;
import com.java.train.business.Dto.ConfirmOrderMQDto;
import com.java.train.business.entity.CanalBean;
import com.java.train.business.entity.ConfirmOrder;
import com.java.train.business.entity.DailyTrainTicket;
import com.java.train.business.entity.KafkaSendResultHandler;
import com.java.train.business.enums.CacheEnum;
import com.java.train.business.mapper.ConfirmOrderMapper;
import com.java.train.business.req.ConfirmOrderDoReq;
import jakarta.annotation.Resource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KafkaUtilsServiceImple {


    private  final Logger log= LoggerFactory.getLogger(KafkaUtilsServiceImple.class);
//    @Resource
//    private KafkaTemplate<String,String> kafkaTemplates;

    @Resource
    private ConfirmOrderServiceImpl confirmOrderService;

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Resource
    private KafkaTemplate<String,String> kafkaTemplate;

//    @Resource
//    private KafkaSendResultHandler kafkaSendResultHandler;

//    public void send(String topic, String key, String value)
//    {
//        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, value);
//        kafkaTemplates.setProducerListener(kafkaSendResultHandler);
//        //CompletableFuture<SendResult<String, String>> send = kafkaTemplate.send(producerRecord);
//        kafkaTemplates.send(topic,key,value);
//    }

    @KafkaListener(topics = {"tickets"})
    public  void listener(ConsumerRecord<String,String>record, Acknowledgment ack)
    {
        try {
            log.info("topic is {},offset is {}, partition is {},value is {}",record.topic(),
                    record.offset(),record.partition(),record.value());
            ack.acknowledge();
            ConfirmOrderMQDto orderMQDto = JSON.parseObject(record.value(), ConfirmOrderMQDto.class);
            confirmOrderService.doConfirm(orderMQDto);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
//        System.out.println("接收到的消息2：" + message);
//        ack.acknowledge();
    }

    @KafkaListener(topics = {"pre_tickets"})
    @Transactional
    public void  listener0(ConsumerRecord<String,String> record, Acknowledgment ack)
    {
        try {
            log.info("topic is {},offset is {}, partition is {},value is {}",record.topic(),
                    record.offset(),record.partition(),record.value());
            ack.acknowledge();
            ConfirmOrder confirmOrder = JSON.parseObject(record.value(), ConfirmOrder.class);
            confirmOrderMapper.insert(confirmOrder);
            log.info("pre队列处理完成，订单已经插入到数据库中：{}",confirmOrder.getId());
            ConfirmOrderMQDto orderMQDto = ConfirmOrderMQDto.builder().date(confirmOrder.getDate())
                    .trainCode(confirmOrder.getTrainCode())
                    .build();
            //2. 插入成功后将订单状态插入队列Tickets队列进行下一步的处理
            kafkaTemplate.send("tickets","confirmOrder",JSON.toJSONString(orderMQDto));
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    @KafkaListener(topics = {"redis"})
    public  void canal_test(ConsumerRecord<String,String> record, Acknowledgment ack)
    {
        String value= record.value();
        try {
            log.info("topic is {},offset is {}, partition is {},value is {}",record.topic(),
                    record.offset(),record.partition(),record.value());
            ack.acknowledge();
            CanalBean canalBean = JSON.parseObject(value, CanalBean.class);
            List<DailyTrainTicket> dailyTrainTickets=canalBean.getData();

            for (DailyTrainTicket dailyTrainTicket:dailyTrainTickets)
            {
                String key= CacheEnum.Ticket_Nums.getCode()+"-"+dailyTrainTicket.getDate()+"-"+
                        dailyTrainTicket.getTrainCode()+"-"+dailyTrainTicket.getStart()+"-"+
                        dailyTrainTicket.getEnd();
                redisTemplate.delete(key);
            }
            log.info("canalBean的数据为{}",canalBean);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        ack.acknowledge();
    }

}

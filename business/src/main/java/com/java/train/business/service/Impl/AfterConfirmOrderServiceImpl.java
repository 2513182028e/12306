package com.java.train.business.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.train.business.entity.ConfirmOrder;
import com.java.train.business.entity.DailyTrainSeat;
import com.java.train.business.entity.DailyTrainTicket;
import com.java.train.business.enums.CacheEnum;
import com.java.train.business.enums.ConfirmOrderStatusEnum;
import com.java.train.business.feign.MemberFeign;
import com.java.train.business.mapper.ConfirmOrderMapper;
import com.java.train.business.mapper.DailyTrainTicketMapper;
import com.java.train.business.req.ConfirmOrderTicketReq;
import com.java.train.business.service.ConfirmOrderService;
import com.java.train.common.context.LoginMemberContext;
import com.java.train.common.req.MemberTicketReq;
import com.java.train.common.resp.CommonResp;
import com.java.train.member.req.TicketSaveReq;
//import io.seata.core.context.RootContext;
//import io.seata.spring.annotation.GlobalTransactional;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AfterConfirmOrderServiceImpl extends ServiceImpl<ConfirmOrderMapper,ConfirmOrder> implements ConfirmOrderService {



    private static final Logger LOG = LoggerFactory.getLogger(AfterConfirmOrderServiceImpl.class);


    @Resource
    private ConfirmOrderMapper ConfirmOrdermapper;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private MemberFeign memberFeign;

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;

    @Resource
    private  DailyTrainTicketServiceImpl dailyTrainTicketService;

    @Resource
    private DailyTrainCarriageServiceImpl dailyTrainCarriageService;

    @Resource
    private DailyTrainSeatServiceImpl dailyTrainSeatService;

    @Resource
    private KafkaTemplate<String,String> kafkaTemplate;
    @GlobalTransactional
    public void  test()
    {
        LOG.info("seata全局事务ID：{}", RootContext.getXID());
        int a=2/0;

    }
    @GlobalTransactional
    public void afterDoFirm(DailyTrainTicket dailyTrainTicket, List<DailyTrainSeat> finalSeatList,List<ConfirmOrderTicketReq> tickets,ConfirmOrder confirmOrders) {
        LOG.info("seata全局事务ID：{}", RootContext.getXID());
        for (int j=0;j<finalSeatList.size();j++)
        {
            DailyTrainSeat dailyTrainSeat = finalSeatList.get(j);
            DailyTrainSeat seatForUpdate = new DailyTrainSeat();
            seatForUpdate.setId(dailyTrainSeat.getId());
            seatForUpdate.setSell(dailyTrainSeat.getSell());
            dailyTrainSeatService.updateById(seatForUpdate);

            //Integer startIndex=4;
            //Integer endIndex=7;
            Integer startIndex=dailyTrainTicket.getStartIndex();
            Integer endIndex=dailyTrainTicket.getEndIndex();
            char[] chars=seatForUpdate.getSell().toCharArray();
            Integer maxStartIndex=endIndex-1;
            Integer minEndIndex=startIndex+1;
            Integer minStartIndex=0;
            for (int i=startIndex;i>=0;i--)
            {
                char cChar=chars[i];
                if(cChar=='1')
                {
                    minStartIndex=i+1;
                    break;
                }
            }
            Integer maxEndIndex=seatForUpdate.getSell().length()-1;
            for (int i=endIndex;i<seatForUpdate.getSell().length();i++)
            {
                char aChar=chars[i];
                if(aChar=='1')
                {
                    maxEndIndex=i;
                    break;
                }
            }
            LOG.info("影响达到站区间："+maxEndIndex+"-"+maxEndIndex);
            dailyTrainTicketMapper.updateCountBySell(seatForUpdate.getDate(),
                    seatForUpdate.getTrainCode(),
                    seatForUpdate.getSeatType(),
                    minStartIndex,
                    maxStartIndex,
                    minEndIndex,
                    maxEndIndex);

            //同步更新DailyTrainTicket余票信息对应的缓存(Mysql和redis数据库的一致性问题，先修改数据库，再删除缓存)，这里会存在redis缓存删除失败的现象
//            String key= CacheEnum.Ticket_Nums.getCode()+"-"+dailyTrainTicket.getDate()+"-"+
//                    dailyTrainTicket.getTrainCode()+"-"+dailyTrainTicket.getStart()+"-"+
//                    dailyTrainTicket.getEnd();
//            redisTemplate.delete(key);
            //这里我们调用Canal整合kafka来处理redis的删除问题,见KakfaServiceImpl出
            MemberTicketReq memberTicketReq = new MemberTicketReq();
            memberTicketReq.setMemberId(confirmOrders.getMemberId());//线程本地变量
            memberTicketReq.setPassengerId(tickets.get(j).getPassengerId());//车票里面有乘客信息
            memberTicketReq.setPassengerName(tickets.get(j).getPassengerName());
            memberTicketReq.setSeatDate(dailyTrainTicket.getDate());
            memberTicketReq.setTrainCode(dailyTrainTicket.getTrainCode());
            memberTicketReq.setCarriageIndex(dailyTrainSeat.getCarriageIndex()); //seat
            memberTicketReq.setSeatRow(dailyTrainSeat.getRow());
            memberTicketReq.setSeatCol(dailyTrainSeat.getCol());
            memberTicketReq.setStartStation(dailyTrainTicket.getStart());
            memberTicketReq.setStartTime(dailyTrainTicket.getStartTime());
            memberTicketReq.setEndStation(dailyTrainTicket.getEnd());
            memberTicketReq.setEndTime(dailyTrainTicket.getEndTime());
            memberTicketReq.setSeatType(dailyTrainSeat.getSeatType());
            CommonResp<Object> commonResp = memberFeign.save(memberTicketReq);
            LOG.info("调用member接口，返回：{}",commonResp);
        }

        //更新订单状态成功
        ConfirmOrder confirmOrder = new ConfirmOrder();
        confirmOrder.setId(confirmOrder.getId());
        confirmOrder.setUpdateTime(new Date());
        confirmOrder.setStatus(ConfirmOrderStatusEnum.SUCCESS.getCode());
        confirmOrderMapper.updateById(confirmOrder);

    }



}

package com.java.train.business.service.Impl;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.hash.BloomFilter;
import com.java.train.business.entity.DailyTrain;
import com.java.train.business.entity.DailyTrainSeat;
import com.java.train.business.entity.TrainStation;
import com.java.train.business.enums.CacheEnum;
import com.java.train.business.enums.SeatTypeEnum;
import com.java.train.business.enums.TrainTypeEnum;
import com.java.train.business.resp.DailyTrainQueryResp;
import com.java.train.business.service.DailyTrainTicketService;
import com.java.train.common.exception.BusinessException;
import com.java.train.common.exception.BusinessExceptionEnum;
import com.java.train.common.resp.PageResp;
import com.java.train.common.util.ShowUtil;
import com.java.train.business.entity.DailyTrainTicket;
import com.java.train.business.mapper.DailyTrainTicketMapper;
import com.java.train.business.req.DailyTrainTicketQueryReq;
import com.java.train.business.req.DailyTrainTicketSaveReq;
import com.java.train.business.resp.DailyTrainTicketQueryResp;
import jakarta.annotation.Resource;
import org.redisson.Redisson;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DailyTrainTicketServiceImpl extends ServiceImpl<DailyTrainTicketMapper,DailyTrainTicket> implements DailyTrainTicketService {



    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainTicketServiceImpl.class);

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private RBloomFilter<String> bloomFilter;

    @Resource
    private  RedissonClient redissonClient;

    @Resource
    private DailyTrainTicketMapper DailyTrainTicketmapper;

    @Resource
    private TrainStationServiceImpl trainStationService;

    @Resource
    private  DailyTrainSeatServiceImpl dailyTrainSeatService;




    public void save(DailyTrainTicketSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainTicket DailyTrainTicketm = BeanUtil.copyProperties(req, DailyTrainTicket.class);
        if (ObjectUtil.isNull(DailyTrainTicketm.getId())) {
            DailyTrainTicketm.setId(ShowUtil.getSnowflakeNextId());
            DailyTrainTicketm.setCreateTime(now);
            DailyTrainTicketm.setUpdateTime(now);
            DailyTrainTicketmapper.insert(DailyTrainTicketm);
        } else {
            DailyTrainTicketm.setUpdateTime(now);
            DailyTrainTicketmapper.updateById(DailyTrainTicketm);
        }
    }

//    public PageResp<DailyTrainTicketQueryResp> queryList0(DailyTrainTicketQueryReq req)
//    {
//        String key=req.getDate()+"-"+req.getStart()+"-"+req.getEnd();
//        if(!bloomFilter.contains(key))
//        {
//            throw  new BusinessException(BusinessExceptionEnum.TICKET_NUMS_ERROR);
//        }//布隆过滤器
//        //查询缓存
//        Object o =() redisTemplate.opsForValue().get(key);
//
//    }

//    @CachePut(value = "DailyTrainTicketService.queryList")
//    public PageResp<DailyTrainTicketQueryResp> queryList2(DailyTrainTicketQueryReq req)
//    {
//        return  queryList(req);
//    }

    //@Cacheable(value = "DailyTrainTicketService.queryList")
    public PageResp<DailyTrainTicketQueryResp> queryList(DailyTrainTicketQueryReq req) {
        String key= CacheEnum.Ticket_Nums.getCode()+"-"+req.getDate()+"-"+req.getTrainCode()+"-"+req.getStart()+"-"+req.getEnd();
        if(!bloomFilter.contains(key))
        {
            throw  new BusinessException(BusinessExceptionEnum.TICKET_NUMS_ERROR);
        }//布隆过滤器
        List<DailyTrainTicket> list = (List<DailyTrainTicket>) redisTemplate.opsForValue().get(key);
        if (list!=null)
        {
            return new PageResp<>((long) list.size(), BeanUtil.copyToList(list,DailyTrainTicketQueryResp.class));
        }
        RLock lock = redissonClient.getLock(key);
        try {
            boolean tried = lock.tryLock(0, TimeUnit.SECONDS);
            if(!tried)
            {
                return queryList(req);
            }
//                LambdaQueryWrapper<DailyTrainTicket> lambdaQueryWrapper=new LambdaQueryWrapper<>();
//                lambdaQueryWrapper.orderByDesc(DailyTrainTicket::getId);
            QueryWrapper<DailyTrainTicket> queryWrapper = new QueryWrapper<>();
            queryWrapper
                    .eq("date",req.getDate())
                    .eq("train_code",req.getTrainCode())
                     .eq("start",req.getStart())
                            .eq("end",req.getEnd())
                                    .orderByDesc("id");
                LOG.info("查询页码：{}", req.getPage());
                LOG.info("每页条数：{}", req.getSize());
                PageHelper.startPage(req.getPage(), req.getSize());
                List<DailyTrainTicket> selectedList = DailyTrainTicketmapper.selectList(queryWrapper);
                PageInfo<DailyTrainTicket> pageInfo = new PageInfo<>(selectedList);
                LOG.info("总行数：{}", pageInfo.getTotal());
                LOG.info("总页数：{}", pageInfo.getPages());
                //List<DailyTrainTicketQueryResp> list = BeanUtil.copyToList(selectedList, DailyTrainTicketQueryResp.class);
                PageResp<DailyTrainTicketQueryResp> pageResp = new PageResp<>();
                pageResp.setTotal(pageInfo.getTotal());
                pageResp.setLists(BeanUtil.copyToList(selectedList,DailyTrainTicketQueryResp.class));
                //存入缓存和布隆过滤器
                redisTemplate.opsForValue().set(key,selectedList);
                bloomFilter.add(key);
                return pageResp;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            if(lock!=null&&lock.isHeldByCurrentThread())
            {
                lock.unlock();
            }
        }
    }

    public void delete(Long id) {
        DailyTrainTicketmapper.deleteById(id);
    }


    @Transactional
    public void  genDaily(DailyTrain dailyTrain, Date date,String trainCode)
    {
        LOG.info("生成日期【{}】车次【{}】的余票信息开始", DateUtil.formatDate(date), trainCode);
        //删除某日某车次的余票信息
        QueryWrapper<DailyTrainTicket> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("date",date)
                .eq("train_code",trainCode);
        DailyTrainTicketmapper.delete(queryWrapper);

        //查出该车次对应的所有的车站信息
        List<TrainStation> trainStationList = trainStationService.selectByTrainCode(trainCode);
        if(CollUtil.isEmpty(trainStationList))
        {
            LOG.info("该车次没有车站基础数据，生成该车次的余票信息结束");
            return;
        }
        DateTime now = new DateTime();
        for (int i=0;i<trainStationList.size();i++)
        {
            TrainStation trainStationStart = trainStationList.get(i);
            BigDecimal sumKM = BigDecimal.ZERO;
            for(int j=i+1;j<trainStationList.size();j++)
            {
                TrainStation trainStationEnd = trainStationList.get(j);
                sumKM=sumKM.add(trainStationEnd.getKm());
                DailyTrainTicket dailyTrainTicket = new DailyTrainTicket();
                dailyTrainTicket.setId(ShowUtil.getSnowflakeNextId());
                dailyTrainTicket.setDate(date);
                dailyTrainTicket.setTrainCode(trainCode);
                dailyTrainTicket.setStart(trainStationStart.getName());
                dailyTrainTicket.setStartPinyin(trainStationStart.getNamePinyin());
                dailyTrainTicket.setStartTime(trainStationStart.getOutTime());
                dailyTrainTicket.setStartIndex(trainStationStart.getIndexes());
                dailyTrainTicket.setEnd(trainStationEnd.getName());
                dailyTrainTicket.setEndPinyin(trainStationEnd.getNamePinyin());
                dailyTrainTicket.setEndTime(trainStationEnd.getInTime());
                dailyTrainTicket.setEndIndex(trainStationEnd.getIndexes());
                int ydz=dailyTrainSeatService.countSeat(date,trainCode, SeatTypeEnum.YDZ.getCode());
                int edz=dailyTrainSeatService.countSeat(date,trainCode,SeatTypeEnum.EDZ.getCode());
                int rw=dailyTrainSeatService.countSeat(date,trainCode,SeatTypeEnum.RW.getCode());
                int yw=dailyTrainSeatService.countSeat(date,trainCode,SeatTypeEnum.YW.getCode());

                //票价=里程之和*座位单价*车次类型系数
                String trainType=dailyTrain.getType();
                BigDecimal priceRate = null; ;
                for (TrainTypeEnum trainTypeEnum:TrainTypeEnum.values()) {
                    if(trainTypeEnum.getCode().equals(trainType))
                    {
                        priceRate=trainTypeEnum.getPriceRate();
                        break;
                    }
                }
                BigDecimal ydzPrice = sumKM.multiply(SeatTypeEnum.YDZ.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal edzPrice = sumKM.multiply(SeatTypeEnum.EDZ.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal rwPrice = sumKM.multiply(SeatTypeEnum.RW.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal ywPrice = sumKM.multiply(SeatTypeEnum.YW.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP);
                dailyTrainTicket.setYdz(ydz);
                dailyTrainTicket.setYdzPrice(ydzPrice);
                dailyTrainTicket.setEdz(edz);
                dailyTrainTicket.setEdzPrice(edzPrice);
                dailyTrainTicket.setRw(rw);
                dailyTrainTicket.setRwPrice(rwPrice);
                dailyTrainTicket.setYw(yw);
                dailyTrainTicket.setYwPrice(ywPrice);
                dailyTrainTicket.setCreateTime(now);
                dailyTrainTicket.setUpdateTime(now);
                DailyTrainTicketmapper.insert(dailyTrainTicket);
            }
            LOG.info("生成日期【{}】车次【{}】的余票信息结束", DateUtil.formatDate(date), trainCode);
        }

    }


    public DailyTrainTicket  selectByUnique(Date date,String trainCode,String start,String end)
    {
            QueryWrapper<DailyTrainTicket> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("date",date)
                    .eq("train_code",trainCode)
                    .eq("start",start)
                    .eq("end",end);
        List<DailyTrainTicket> lists = DailyTrainTicketmapper.selectList(queryWrapper);
        if(CollUtil.isNotEmpty(lists))
        {
            return lists.get(0);
        }
        else {
            return  null;
        }
    }
}

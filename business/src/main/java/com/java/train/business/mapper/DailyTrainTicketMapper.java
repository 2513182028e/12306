package com.java.train.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java.train.business.entity.DailyTrainTicket;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

@Mapper

public interface DailyTrainTicketMapper extends BaseMapper<DailyTrainTicket>   {

    void updateCountBySell(Date date
                           ,String trainCode
                           ,String seatTypeCode
                            ,int minStartIndex
                            ,int maxStartIndex
                           ,int minEndIndex
                            ,int maxEndIndex);



    }
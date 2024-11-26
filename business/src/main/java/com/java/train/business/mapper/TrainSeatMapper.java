package com.java.train.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java.train.business.entity.TrainSeat;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrainSeatMapper extends BaseMapper<TrainSeat> {


}

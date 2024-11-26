package com.java.train.business.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java.train.business.entity.TrainStation;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TrainStationMapper extends  BaseMapper<TrainStation> {


    List<TrainStation> findByCode(String trainCode);
}

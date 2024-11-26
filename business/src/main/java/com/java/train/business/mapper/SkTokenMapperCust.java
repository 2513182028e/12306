package com.java.train.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java.train.business.entity.SkToken;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;


@Mapper
public interface SkTokenMapperCust extends BaseMapper<SkToken> {


    int decrease(Date date, String trainCode,int DecreaseCount);
}

package com.java.train.business.service.Impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.java.train.business.entity.TrainStation;
import com.java.train.business.mapper.TrainStationMapper;
import com.java.train.business.req.TrainStationQueryReq;
import com.java.train.business.req.TrainStationSaveReq;
import com.java.train.business.resp.TrainStationQueryResp;
import com.java.train.business.service.TrainStationService;
import com.java.train.common.exception.BusinessException;
import com.java.train.common.exception.BusinessExceptionEnum;
import com.java.train.common.resp.PageResp;
import com.java.train.common.util.ShowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainStationServiceImpl extends ServiceImpl<TrainStationMapper,TrainStation> implements TrainStationService {


    private static final Logger LOG = LoggerFactory.getLogger(TrainStationServiceImpl.class);

    @Resource
    private TrainStationMapper trainStationMapper;


    public void save(TrainStationSaveReq req) {
        DateTime now = DateTime.now();
        TrainStation trainStation = BeanUtil.copyProperties(req, TrainStation.class);
        if (ObjectUtil.isNull(trainStation.getId())) {

            // 保存之前，先校验唯一键是否存在
            TrainStation trainStationDB = selectByUnique(req.getTrainCode(), req.getIndex());
            if (ObjectUtil.isNotEmpty(trainStationDB)) {
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR);
            }
            // 保存之前，先校验唯一键是否存在
            trainStationDB = selectByUnique(req.getTrainCode(), req.getName());
            if (ObjectUtil.isNotEmpty(trainStationDB)) {
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR);
            }

            trainStation.setId(ShowUtil.getSnowflakeNextId());
            trainStation.setCreateTime(now);
            trainStation.setUpdateTime(now);
            trainStationMapper.insert(trainStation);
        } else {
            trainStation.setUpdateTime(now);
            trainStationMapper.updateById(trainStation);
        }
    }

    private TrainStation selectByUnique(String trainCode, Integer index) {

        QueryWrapper<TrainStation> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("train_code",trainCode).eq("index",index);
        List<TrainStation> trainStationList = trainStationMapper.selectList(queryWrapper);
        if(!ObjectUtil.isNull(trainStationList))
        {
            return  trainStationList.get(0);
        }
        return  null;
    }
    private TrainStation selectByUnique(String trainCode, String name) {

        QueryWrapper<TrainStation> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("train_code",trainCode).eq("name",name);
        List<TrainStation> trainStationList = trainStationMapper.selectList(queryWrapper);
        if(!ObjectUtil.isNull(trainStationList))
        {
            return  trainStationList.get(0);
        }
        return  null;
    }

    public PageResp<TrainStationQueryResp> queryList(TrainStationQueryReq req) {
//        TrainStationExample trainStationExample = new TrainStationExample();
//        trainStationExample.setOrderByClause("train_code asc, `index` asc");
//        TrainStationExample.Criteria criteria = trainStationExample.createCriteria();
//        if (ObjectUtil.isNotEmpty(req.getTrainCode())) {
//            criteria.andTrainCodeEqualTo(req.getTrainCode());
//        }
        QueryWrapper<TrainStation> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderBy(true,true,"train_code","index");

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<TrainStation> trainStationList = trainStationMapper.selectList(queryWrapper);
        PageInfo<TrainStation> pageInfo = new PageInfo<>(trainStationList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<TrainStationQueryResp> list = BeanUtil.copyToList(trainStationList, TrainStationQueryResp.class);

        PageResp<TrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setLists(list);
        return pageResp;
    }

    public void delete(Long id) {
        trainStationMapper.deleteById(id);
    }

    public List<TrainStation> selectByTrainCode(String trainCode) {
//        TrainStationExample trainStationExample = new TrainStationExample();
//        trainStationExample.setOrderByClause("`index` asc");
//        trainStationExample.createCriteria().andTrainCodeEqualTo(trainCode);
        //LOG.info("trainCode的数值为:{}",trainCode);
        QueryWrapper<TrainStation> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("train_code",trainCode)
                .orderByAsc("indexes");

        return trainStationMapper.selectList(queryWrapper);

    }

    public  int countByTrainCode(String trainCode)
    {
        QueryWrapper<TrainStation> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("train_code",trainCode);
        long aLong = trainStationMapper.selectCount(queryWrapper);
        return (int) aLong;
    }

}

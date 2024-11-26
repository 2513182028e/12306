package com.java.train.business.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.java.train.business.entity.TrainCarriage;
import com.java.train.business.enums.SeatColEnum;
import com.java.train.business.mapper.TrainCarriageMapper;
import com.java.train.business.req.TrainCarriageQueryReq;
import com.java.train.business.req.TrainCarriageSaveReq;
import com.java.train.business.resp.TrainCarriageQueryResp;
import com.java.train.business.service.TrainCarriageService;
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
public class TrainCarriageServiceImpl extends ServiceImpl<TrainCarriageMapper, TrainCarriage> implements TrainCarriageService {



    private static final Logger LOG = LoggerFactory.getLogger(TrainStationServiceImpl.class);


   // private static final Logger LOG = LoggerFactory.getLogger(TrainCarriageService.class);

    @Resource
    private TrainCarriageMapper trainCarriageMapper;

    public void save(TrainCarriageSaveReq req) {
        DateTime now = DateTime.now();

        // 自动计算出列数和总座位数
        List<SeatColEnum> seatColEnums = SeatColEnum.getColsByType(req.getSeatType());
        req.setColCount(seatColEnums.size());
        req.setSeatCount(req.getColCount() * req.getRowCount());

        TrainCarriage trainCarriage = BeanUtil.copyProperties(req, TrainCarriage.class);
        if (ObjectUtil.isNull(trainCarriage.getId())) {

            // 保存之前，先校验唯一键是否存在
            TrainCarriage trainCarriageDB = selectByUnique(req.getTrainCode(), req.getIndex());
            if (ObjectUtil.isNotEmpty(trainCarriageDB)) {
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR);
            }

            trainCarriage.setId(ShowUtil.getSnowflakeNextId());
            trainCarriage.setCreateTime(now);
            trainCarriage.setUpdateTime(now);
            trainCarriageMapper.insert(trainCarriage);
        } else {
            trainCarriage.setUpdateTime(now);
            trainCarriageMapper.updateById(trainCarriage);
        }
    }

    private TrainCarriage selectByUnique(String trainCode, Integer index) {
//        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
//        trainCarriageExample.createCriteria()
//                .andTrainCodeEqualTo(trainCode)
//                .andIndexEqualTo(index);
//        List<TrainCarriage> list = trainCarriageMapper.selectByExample(trainCarriageExample);
//        if (CollUtil.isNotEmpty(list)) {
//            return list.get(0);
//        } else {
//            return null;
//        }
        QueryWrapper<TrainCarriage> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("train_code",trainCode)
                .eq("index",index);
        List<TrainCarriage> trainCarriages = trainCarriageMapper.selectList(queryWrapper);
        if(CollUtil.isNotEmpty(trainCarriages))
        {
            return trainCarriages.get(0);
        }
        return null;
    }

    public PageResp<TrainCarriageQueryResp> queryList(TrainCarriageQueryReq req) {
//        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
//        trainCarriageExample.setOrderByClause("train_code asc, `index` asc");
//        TrainCarriageExample.Criteria criteria = trainCarriageExample.createCriteria();
//        if (ObjectUtil.isNotEmpty(req.getTrainCode())) {
//            criteria.andTrainCodeEqualTo(req.getTrainCode());
//        }
        QueryWrapper<TrainCarriage> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByAsc("train_code")
                        .orderByAsc("index");

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<TrainCarriage> trainCarriages = trainCarriageMapper.selectList(queryWrapper);

        PageInfo<TrainCarriage> pageInfo = new PageInfo<>(trainCarriages);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<TrainCarriageQueryResp> list = BeanUtil.copyToList(trainCarriages, TrainCarriageQueryResp.class);

        PageResp<TrainCarriageQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setLists(list);
        return pageResp;
    }

    public void delete(Long id) {
        trainCarriageMapper.deleteById(id);
    }

    public List<TrainCarriage> selectByTrainCode(String trainCode) {

        QueryWrapper<TrainCarriage> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("train_code",trainCode)
                .orderByAsc("indexes");
        return trainCarriageMapper.selectList(queryWrapper);

    }




}

package com.java.train.business.service.Impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.java.train.business.entity.TrainCarriage;
import com.java.train.business.entity.TrainSeat;
import com.java.train.business.enums.SeatColEnum;
import com.java.train.business.mapper.TrainSeatMapper;
import com.java.train.business.req.TrainSeatQueryReq;
import com.java.train.business.req.TrainSeatSaveReq;
import com.java.train.business.resp.TrainSeatQueryResp;
import com.java.train.business.service.TrainSeatService;
import com.java.train.common.resp.PageResp;
import com.java.train.common.util.ShowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainSeatServiceImpl extends ServiceImpl<TrainSeatMapper, TrainSeat> implements TrainSeatService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainSeatServiceImpl.class);


    @Resource
    private TrainSeatMapper trainSeatMapper;

    @Resource
    private TrainCarriageServiceImpl trainCarriageService;


    public void save(TrainSeatSaveReq req) {
        DateTime now = DateTime.now();
        TrainSeat trainSeat = BeanUtil.copyProperties(req, TrainSeat.class);
        if (ObjectUtil.isNull(trainSeat.getId())) {
            trainSeat.setId(ShowUtil.getSnowflakeNextId());
            trainSeat.setCreateTime(now);
            trainSeat.setUpdateTime(now);
            trainSeatMapper.insert(trainSeat);
        } else {
            trainSeat.setUpdateTime(now);
            trainSeatMapper.updateById(trainSeat);
        }
    }


    public PageResp<TrainSeatQueryResp> queryList(TrainSeatQueryReq req) {

        QueryWrapper<TrainSeat> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByAsc("train_code")
                        .orderByAsc("carriage_index")
                                .orderByAsc("carriage_seat_index");

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<TrainSeat> trainSeatList = trainSeatMapper.selectList(queryWrapper);

        PageInfo<TrainSeat> pageInfo = new PageInfo<>(trainSeatList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<TrainSeatQueryResp> list = BeanUtil.copyToList(trainSeatList, TrainSeatQueryResp.class);

        PageResp<TrainSeatQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setLists(list);
        return pageResp;
    }

    public void delete(Long id) {
        trainSeatMapper.deleteById(id);
    }


    // 产生新的座位
    @Transactional
    public void genTrainSeat(String trainCode) {
        DateTime now = DateTime.now();
        // 清空当前车次下的所有的座位记录,清除的是trainseat表，不是trainCarriage表

        QueryWrapper<TrainSeat> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("train_code",trainCode);
        trainSeatMapper.delete(queryWrapper);


        // 查找当前车次下的所有的车厢
        List<TrainCarriage> carriageList = trainCarriageService.selectByTrainCode(trainCode);
        LOG.info("当前车次下的车厢数：{}", carriageList.size());

        // 循环生成每个车厢的座位
        for (TrainCarriage trainCarriage : carriageList) {
            // 拿到车厢数据：行数、座位类型(得到列数)
            Integer rowCount = trainCarriage.getRowCount();
            String seatType = trainCarriage.getSeatType();
            int seatIndex = 1;

            // 根据车厢的座位类型，筛选出所有的列，比如车箱类型是一等座，则筛选出columnList={ACDF}
            List<SeatColEnum> colEnumList = SeatColEnum.getColsByType(seatType);
            LOG.info("根据车厢的座位类型，筛选出所有的列：{}", colEnumList);

            // 循环行数
            for (int row = 1; row <= rowCount; row++) {
                // 循环列数
                for (SeatColEnum seatColEnum : colEnumList) {
                    // 构造座位数据并保存数据库
                    TrainSeat trainSeat = new TrainSeat();
                    trainSeat.setId(ShowUtil.getSnowflakeNextId());
                    trainSeat.setTrainCode(trainCode);
                    trainSeat.setCarriageIndex(trainCarriage.getIndex());
                    trainSeat.setRow(StrUtil.fillBefore(String.valueOf(row), '0', 2));
                    trainSeat.setCol(seatColEnum.getCode());
                    trainSeat.setSeatType(seatType);
                    trainSeat.setCarriageSeatIndex(seatIndex++);
                    trainSeat.setCreateTime(now);
                    trainSeat.setUpdateTime(now);
                    trainSeatMapper.insert(trainSeat);
                }
            }
        }
    }

    public List<TrainSeat> selectByTrainCode(String trainCode) {
//        TrainSeatExample trainSeatExample = new TrainSeatExample();
//        trainSeatExample.setOrderByClause("`id` asc");
//        TrainSeatExample.Criteria criteria = trainSeatExample.createCriteria();
//        criteria.andTrainCodeEqualTo(trainCode);
//        return trainSeatMapper.selectByExample(trainSeatExample);

        QueryWrapper<TrainSeat> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("train_code",trainCode)
                .orderByAsc("id");
        return  trainSeatMapper.selectList(queryWrapper);

    }

}

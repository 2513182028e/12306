package com.java.train.business.service.Impl;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.java.train.business.entity.TrainStation;
import com.java.train.business.mapper.DailyTrainMapper;
import com.java.train.business.service.DailyTrainStationService;
import com.java.train.common.resp.PageResp;
import com.java.train.common.util.ShowUtil;
import com.java.train.business.entity.DailyTrainStation;
import com.java.train.business.mapper.DailyTrainStationMapper;
import com.java.train.business.req.DailyTrainStationQueryReq;
import com.java.train.business.req.DailyTrainStationSaveReq;
import com.java.train.business.resp.DailyTrainStationQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
public class DailyTrainStationServiceImpl extends ServiceImpl<DailyTrainStationMapper,DailyTrainStation> implements DailyTrainStationService {



    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainStationServiceImpl.class);

    @Resource
    private DailyTrainStationMapper DailyTrainStationmapper;

    @Resource
    private  TrainStationServiceImpl trainStationService;

    public void save(DailyTrainStationSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainStation DailyTrainStationm = BeanUtil.copyProperties(req, DailyTrainStation.class);
        if (ObjectUtil.isNull(DailyTrainStationm.getId())) {
            DailyTrainStationm.setId(ShowUtil.getSnowflakeNextId());
            DailyTrainStationm.setCreateTime(now);
            DailyTrainStationm.setUpdateTime(now);
            DailyTrainStationmapper.insert(DailyTrainStationm);
        } else {
            DailyTrainStationm.setUpdateTime(now);
            DailyTrainStationmapper.updateById(DailyTrainStationm);
        }
    }

    public PageResp<DailyTrainStationQueryResp> queryList(DailyTrainStationQueryReq req) {
    LambdaQueryWrapper<DailyTrainStation> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(DailyTrainStation::getId);

            LOG.info("查询页码：{}", req.getPage());
            LOG.info("每页条数：{}", req.getSize());
            PageHelper.startPage(req.getPage(), req.getSize());
            List<DailyTrainStation> selectedList = DailyTrainStationmapper.selectList(lambdaQueryWrapper);
            PageInfo<DailyTrainStation> pageInfo = new PageInfo<>(selectedList);
                LOG.info("总行数：{}", pageInfo.getTotal());
                LOG.info("总页数：{}", pageInfo.getPages());

                List<DailyTrainStationQueryResp> list = BeanUtil.copyToList(selectedList, DailyTrainStationQueryResp.class);

                    PageResp<DailyTrainStationQueryResp> pageResp = new PageResp<>();
                        pageResp.setTotal(pageInfo.getTotal());
                        pageResp.setLists(list);
                        return pageResp;

    }


    public void delete(Long id) {
        DailyTrainStationmapper.deleteById(id);
    }

    public void genDaily(Date date, String code) {

    LOG.info("生成日期【{}】车次【{}】的车站信息开始", DateUtil.formatDate(date),code);
        //第一步，删除该日期下的全部数据
        QueryWrapper<DailyTrainStation> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("date",date)
                .eq("train_code",code);
        DailyTrainStationmapper.delete(queryWrapper);
        //第二步，查出该日期下的所有的车站信息
        List<TrainStation> trainStationList = trainStationService.selectByTrainCode(code);
        if(CollUtil.isEmpty(trainStationList))
        {
            LOG.info("该车次没有车站基础数据，生成该车次的车站信息结束");
            return;
        }
        //给每日数据进行赋值
        for (TrainStation trainStation:
             trainStationList) {
            DateTime now=new DateTime();
            DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(trainStation, DailyTrainStation.class);
            dailyTrainStation.setId(ShowUtil.getSnowflakeNextId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStation.setDate(date);
            DailyTrainStationmapper.insert(dailyTrainStation);
        }
        LOG.info("生成日期【{}】车次【{}】的车站信息结束", DateUtil.formatDate(date), code);

    }
}

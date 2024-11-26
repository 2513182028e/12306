package com.java.train.business.service.Impl;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.java.train.business.entity.DailyTrainCarriage;
import com.java.train.business.entity.TrainSeat;
import com.java.train.business.service.DailyTrainSeatService;
import com.java.train.common.resp.PageResp;
import com.java.train.common.util.ShowUtil;
import com.java.train.business.entity.DailyTrainSeat;
import com.java.train.business.mapper.DailyTrainSeatMapper;
import com.java.train.business.req.DailyTrainSeatQueryReq;
import com.java.train.business.req.DailyTrainSeatSaveReq;
import com.java.train.business.resp.DailyTrainSeatQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
public class DailyTrainSeatServiceImpl extends ServiceImpl<DailyTrainSeatMapper,DailyTrainSeat> implements DailyTrainSeatService {



    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainSeatServiceImpl.class);


    @Resource
    private DailyTrainSeatMapper DailyTrainSeatmapper;

    @Resource
    private TrainStationServiceImpl trainStationService;

    @Resource
    private  TrainSeatServiceImpl trainSeatService;


    public void save(DailyTrainSeatSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainSeat DailyTrainSeatm = BeanUtil.copyProperties(req, DailyTrainSeat.class);
        if (ObjectUtil.isNull(DailyTrainSeatm.getId())) {
            DailyTrainSeatm.setId(ShowUtil.getSnowflakeNextId());
            DailyTrainSeatm.setCreateTime(now);
            DailyTrainSeatm.setUpdateTime(now);
            DailyTrainSeatmapper.insert(DailyTrainSeatm);
        } else {
            DailyTrainSeatm.setUpdateTime(now);
            DailyTrainSeatmapper.updateById(DailyTrainSeatm);
        }
    }

    public PageResp<DailyTrainSeatQueryResp> queryList(DailyTrainSeatQueryReq req) {
    LambdaQueryWrapper<DailyTrainSeat> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(DailyTrainSeat::getId);

            LOG.info("查询页码：{}", req.getPage());
            LOG.info("每页条数：{}", req.getSize());
            PageHelper.startPage(req.getPage(), req.getSize());
            List<DailyTrainSeat> selectedList = DailyTrainSeatmapper.selectList(lambdaQueryWrapper);
            PageInfo<DailyTrainSeat> pageInfo = new PageInfo<>(selectedList);
                LOG.info("总行数：{}", pageInfo.getTotal());
                LOG.info("总页数：{}", pageInfo.getPages());

                List<DailyTrainSeatQueryResp> list = BeanUtil.copyToList(selectedList, DailyTrainSeatQueryResp.class);

                    PageResp<DailyTrainSeatQueryResp> pageResp = new PageResp<>();
                        pageResp.setTotal(pageInfo.getTotal());
                        pageResp.setLists(list);
                        return pageResp;

    }



    public void delete(Long id) {
        DailyTrainSeatmapper.deleteById(id);
    }

    public void  genDaily(Date date,String trainCode)
    {
        //删除该日期下车次的所有座位信息
        QueryWrapper<DailyTrainSeat >queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("date",date)
                .eq("train_code",trainCode);
        DailyTrainSeatmapper.delete(queryWrapper);

        //查找该列火车进过的站点数
        int len = trainStationService.countByTrainCode(trainCode);
        String sell= StrUtil.fillBefore("",'0',len-1);
        List<TrainSeat> trainSeats = trainSeatService.selectByTrainCode(trainCode);
        if(CollUtil.isEmpty(trainSeats))
        {
            LOG.info("该车次没有座位基础数据，生成该车次的座位信息结束");
            return;
        }
        for (TrainSeat trainSeat :
                trainSeats) {
            DateTime now = new DateTime();
            DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(trainSeat, DailyTrainSeat.class);
            dailyTrainSeat.setId(ShowUtil.getSnowflakeNextId());
            dailyTrainSeat.setSell(sell);
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setDate(date);
            DailyTrainSeatmapper.insert(dailyTrainSeat);
        }
        LOG.info("生成日期【{}】车次【{}】的座位信息结束", DateUtil.formatDate(date), trainCode);
    }
    public int countSeat(Date date,String trainCode,String seatType)
    {
        QueryWrapper<DailyTrainSeat> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("date",date)
                .eq("train_code",trainCode)
                .eq("seat_type",seatType);
        long aLong = DailyTrainSeatmapper.selectCount(queryWrapper);
        if(aLong==0L)
        {
                return  -1;
        }
        return (int)aLong;
    }


    public  List<DailyTrainSeat> selectByCarriage(Date date, String trainCode, Integer carriageIndex)
    {
        QueryWrapper<DailyTrainSeat> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("date",date)
                .eq("train_code",trainCode)
                .eq("carriage_index",carriageIndex)
                .orderByAsc("carriage_seat_index");
        return DailyTrainSeatmapper.selectList(queryWrapper);
    }


}

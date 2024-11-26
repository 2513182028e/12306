package com.java.train.business.service.Impl;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.java.train.business.entity.Train;
import com.java.train.business.mapper.TrainMapper;
import com.java.train.business.service.DailyTrainService;
import com.java.train.common.resp.PageResp;
import com.java.train.common.util.ShowUtil;
import com.java.train.business.entity.DailyTrain;
import com.java.train.business.mapper.DailyTrainMapper;
import com.java.train.business.req.DailyTrainQueryReq;
import com.java.train.business.req.DailyTrainSaveReq;
import com.java.train.business.resp.DailyTrainQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
public class DailyTrainServiceImpl extends ServiceImpl<DailyTrainMapper,DailyTrain> implements DailyTrainService {



    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainServiceImpl.class);


    @Resource
    private DailyTrainMapper DailyTrainmapper;

    @Resource
    private TrainServiceImpl trainService;

    @Resource
    private DailyTrainStationServiceImpl dailyTrainStationService;

    @Resource
    private  DailyTrainCarriageServiceImpl dailyTrainCarriageService;

    @Resource
    private  DailyTrainSeatServiceImpl dailyTrainSeatService;

    @Resource
    private DailyTrainTicketServiceImpl dailyTrainTicketService;




    public void save(DailyTrainSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrain DailyTrainm = BeanUtil.copyProperties(req, DailyTrain.class);
        if (ObjectUtil.isNull(DailyTrainm.getId())) {
            DailyTrainm.setId(ShowUtil.getSnowflakeNextId());
            DailyTrainm.setCreateTime(now);
            DailyTrainm.setUpdateTime(now);
            DailyTrainmapper.insert(DailyTrainm);
        } else {
            DailyTrainm.setUpdateTime(now);
            DailyTrainmapper.updateById(DailyTrainm);
        }
    }

    public PageResp<DailyTrainQueryResp> queryList(DailyTrainQueryReq req) {
    LambdaQueryWrapper<DailyTrain> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(DailyTrain::getId);

            LOG.info("查询页码：{}", req.getPage());
            LOG.info("每页条数：{}", req.getSize());
            PageHelper.startPage(req.getPage(), req.getSize());
            List<DailyTrain> selectedList = DailyTrainmapper.selectList(lambdaQueryWrapper);
            PageInfo<DailyTrain> pageInfo = new PageInfo<>(selectedList);
                LOG.info("总行数：{}", pageInfo.getTotal());
                LOG.info("总页数：{}", pageInfo.getPages());

                List<DailyTrainQueryResp> list = BeanUtil.copyToList(selectedList, DailyTrainQueryResp.class);

                    PageResp<DailyTrainQueryResp> pageResp = new PageResp<>();
                        pageResp.setTotal(pageInfo.getTotal());
                        pageResp.setLists(list);
                        return pageResp;

    }



    public void delete(Long id) {
        DailyTrainmapper.deleteById(id);
    }

    /**
     *              生成某日所有车次，包括车次，车站，车厢，座位
     * &#064;Params  date  **/
    public void genDaily(Date date)
    {
        List<Train> trainList = trainService.selectAll();
        if(CollUtil.isEmpty(trainList))
        {
            LOG.info("没有车次基础数据，任务结束");
            return;
        }
        for (Train train: trainList)
        {
                getDailyTrain(date,train);
        }

    }
    public void  getDailyTrain(Date date,Train train)
    {
        DateTime now = DateTime.now();


        //删除该车次已有的数据
        QueryWrapper<DailyTrain> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("code",train.getCode());
        queryWrapper.eq("date",date);
        DailyTrainmapper.delete(queryWrapper);

        //生成新的车次信息
        DailyTrain dailyTrain = BeanUtil.copyProperties(train, DailyTrain.class);
        dailyTrain.setId(ShowUtil.getSnowflakeNextId());
        dailyTrain.setUpdateTime(now);
        dailyTrain.setCreateTime(now);
        dailyTrain.setDate(date);
        DailyTrainmapper.insert(dailyTrain);
        //LOG.info("dailyTrain的车次:{}",dailyTrain);
        //生成该车次的车站数据
        dailyTrainStationService.genDaily(date,train.getCode());

        //生成该车次的车厢数据
        dailyTrainCarriageService.genDaily(date,train.getCode());

        //生成该车次的座位数据
        dailyTrainSeatService.genDaily(date,train.getCode());

        //生成该车次的余票信息
        dailyTrainTicketService.genDaily(dailyTrain,date,train.getCode());
    }

}

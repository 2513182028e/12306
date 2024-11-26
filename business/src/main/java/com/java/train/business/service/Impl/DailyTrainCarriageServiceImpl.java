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
import com.java.train.business.entity.DailyTrainStation;
import com.java.train.business.entity.TrainCarriage;
import com.java.train.business.entity.TrainStation;
import com.java.train.business.service.DailyTrainCarriageService;
import com.java.train.common.resp.PageResp;
import com.java.train.common.util.ShowUtil;
import com.java.train.business.entity.DailyTrainCarriage;
import com.java.train.business.mapper.DailyTrainCarriageMapper;
import com.java.train.business.req.DailyTrainCarriageQueryReq;
import com.java.train.business.req.DailyTrainCarriageSaveReq;
import com.java.train.business.resp.DailyTrainCarriageQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
public class DailyTrainCarriageServiceImpl extends ServiceImpl<DailyTrainCarriageMapper,DailyTrainCarriage> implements DailyTrainCarriageService {



    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainCarriageServiceImpl.class);


    @Resource
    private DailyTrainCarriageMapper DailyTrainCarriagemapper;

    @Resource
    private  TrainCarriageServiceImpl trainCarriageService;



    public void save(DailyTrainCarriageSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainCarriage DailyTrainCarriagem = BeanUtil.copyProperties(req, DailyTrainCarriage.class);
        if (ObjectUtil.isNull(DailyTrainCarriagem.getId())) {
            DailyTrainCarriagem.setId(ShowUtil.getSnowflakeNextId());
            DailyTrainCarriagem.setCreateTime(now);
            DailyTrainCarriagem.setUpdateTime(now);
            DailyTrainCarriagemapper.insert(DailyTrainCarriagem);
        } else {
            DailyTrainCarriagem.setUpdateTime(now);
            DailyTrainCarriagemapper.updateById(DailyTrainCarriagem);
        }
    }

    public PageResp<DailyTrainCarriageQueryResp> queryList(DailyTrainCarriageQueryReq req) {
    LambdaQueryWrapper<DailyTrainCarriage> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(DailyTrainCarriage::getId);

            LOG.info("查询页码：{}", req.getPage());
            LOG.info("每页条数：{}", req.getSize());
            PageHelper.startPage(req.getPage(), req.getSize());
            List<DailyTrainCarriage> selectedList = DailyTrainCarriagemapper.selectList(lambdaQueryWrapper);
            PageInfo<DailyTrainCarriage> pageInfo = new PageInfo<>(selectedList);
                LOG.info("总行数：{}", pageInfo.getTotal());
                LOG.info("总页数：{}", pageInfo.getPages());

                List<DailyTrainCarriageQueryResp> list = BeanUtil.copyToList(selectedList, DailyTrainCarriageQueryResp.class);

                    PageResp<DailyTrainCarriageQueryResp> pageResp = new PageResp<>();
                        pageResp.setTotal(pageInfo.getTotal());
                        pageResp.setLists(list);
                        return pageResp;

    }



    public void delete(Long id) {
        DailyTrainCarriagemapper.deleteById(id);
    }



    public void genDaily(Date date, String code) {

        LOG.info("生成日期【{}】车次【{}】的车厢信息开始", DateUtil.formatDate(date),code);
        //第一步，删除该日期下的全部数据
        QueryWrapper<DailyTrainCarriage> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("date",date)
                .eq("train_code",code);
        DailyTrainCarriagemapper.delete(queryWrapper);
        //第二步，查出该日期下的所有的车站信息
        List<TrainCarriage> trainCarriages = trainCarriageService.selectByTrainCode(code);
        if(CollUtil.isEmpty(trainCarriages))
        {
            LOG.info("该车次没有车厢基础数据，生成该车次的车厢信息结束");
            return;
        }
        LOG.info("车厢信息：{}",trainCarriages);
        //给每日数据进行赋值
        for (TrainCarriage trainCarriage:
                trainCarriages) {
            DateTime now=new DateTime();
            DailyTrainCarriage dailyTrainCarriage = BeanUtil.copyProperties(trainCarriage, DailyTrainCarriage.class);
            dailyTrainCarriage.setId(ShowUtil.getSnowflakeNextId());
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriage.setDate(date);
            DailyTrainCarriagemapper.insert(dailyTrainCarriage);
        }
        LOG.info("生成日期【{}】车次【{}】的车厢信息结束", DateUtil.formatDate(date), code);

    }


    public  List<DailyTrainCarriage> selectBySeatType(Date date,String trainCode,String seatType)
    {
        QueryWrapper<DailyTrainCarriage> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("date",date)
                .eq("train_code",trainCode)
                .eq("seat_type",seatType);
        return DailyTrainCarriagemapper.selectList(queryWrapper);
    }
}

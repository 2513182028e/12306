package com.java.train.business.job;

import cn.hutool.core.date.DateUtil;
import com.java.train.business.feign.BusinessFeign;
import com.java.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@DisallowConcurrentExecution
public class DailyTrainJob implements Job {

    private static  final Logger LOG= LoggerFactory.getLogger(DailyTrainJob.class);

    @Resource
    private BusinessFeign businessFeign;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        LOG.info("生成每日车次开始");
        Date date = new Date();
        Date dateTime = DateUtil.offsetDay(date, 15).toJdkDate();
        CommonResp<Object> objectCommonResp = businessFeign.genDaily(dateTime);
        LOG.info("生成15天后的车次数据结果：{}",objectCommonResp);

    }
}
package com.java.train.business.controller;
import cn.hutool.core.date.DateTime;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.java.train.business.entity.Train;
import com.java.train.business.entity.TrainStation;
import com.java.train.business.mapper.TrainMapper;
import com.java.train.business.mapper.TrainStationMapper;
import com.java.train.business.req.ConfirmOrderDoReq;
import com.java.train.business.service.Impl.AfterConfirmOrderServiceImpl;
import com.java.train.business.service.Impl.BeforeConfirmOrderServiceImpl;
import com.java.train.business.service.Impl.ConfirmOrderServiceImpl;
import com.java.train.business.service.Impl.DailyTrainServiceImpl;
import com.java.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RequestMapping("/confirmOrder")
@RestController
public class ConfirmOrderController {

    private  static final Logger LOG= LoggerFactory.getLogger(ConfirmOrderController.class);
    @Resource
    private ConfirmOrderServiceImpl confirmOrderService;

    @Resource
    private BeforeConfirmOrderServiceImpl beforeConfirmOrderService;

    @Resource
    private AfterConfirmOrderServiceImpl afterConfirmOrderService;

    @Resource
    private DailyTrainServiceImpl dailyTrainService;

    @Resource
    private TrainStationMapper trainStationMapper;

    @Resource
    private TrainMapper trainMapper;

//    @SentinelResource(value = "doConfirm",blockHandler = "doConfirmBlock")
    @PostMapping("/do")
    //@SentinelResource(value = "doConfirm",blockHandler = "doConfirmBlock")
    public CommonResp<Object> doConfirm(@Valid @RequestBody ConfirmOrderDoReq req) throws InterruptedException {


        /**
         * 验证码环节**/
        Long id = beforeConfirmOrderService.doBeforeComfirmOrder(req);
        return new CommonResp<>(String.valueOf(id));
    }

    @GetMapping("/query-line-count/{id}")
    public Integer queryLineCount(@PathVariable("id") Long id)
    {
                Integer count=confirmOrderService.queryLineCount(id);
                return count;
    }

    @GetMapping("/test")
    public String test()
    {
        afterConfirmOrderService.test();
        return null;
    }

    @GetMapping("/tests")
    public String tests() throws ParseException {
        String da="2023-03-01";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateTime date = new DateTime(simpleDateFormat.parse(da).getTime());
        LOG.info("默认的日期为:{}",date);
        //List<Train> trains = trainMapper.selectList(null);
        dailyTrainService.genDaily(date);

        return "success";
    }

//    public CommonResp<Object> doConfirmBlock(ConfirmOrderDoReq req, BlockException e)
//    {
     // 熔断降级方法
//        LOG.info("购票请求被限流：{}",req);
////        throw  new BusinessException(BusinessExceptionEnum.CONFIRM_OREDER_FLOW_EXCEPTION);
//        CommonResp<Object> commonResp=new CommonResp<>();
//        commonResp.setSuccess(false);
//        commonResp.setMessage(BusinessExceptionEnum.CONFIRM_OREDER_FLOW_EXCEPTION.getDesc());
//        return commonResp;
//    }


}

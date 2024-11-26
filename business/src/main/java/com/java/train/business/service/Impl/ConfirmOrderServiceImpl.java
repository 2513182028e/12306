package com.java.train.business.service.Impl;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.java.train.business.Dto.ConfirmOrderMQDto;
import com.java.train.business.entity.DailyTrainCarriage;
import com.java.train.business.entity.DailyTrainSeat;
import com.java.train.business.entity.DailyTrainTicket;
import com.java.train.business.enums.*;
import com.java.train.business.req.ConfirmOrderDoReq;
import com.java.train.business.req.ConfirmOrderTicketReq;
import com.java.train.business.service.ConfirmOrderService;
import com.java.train.common.exception.BusinessException;
import com.java.train.common.exception.BusinessExceptionEnum;
import com.java.train.common.resp.PageResp;
import com.java.train.common.util.ShowUtil;
import com.java.train.business.entity.ConfirmOrder;
import com.java.train.business.mapper.ConfirmOrderMapper;
import com.java.train.business.req.ConfirmOrderQueryReq;
import com.java.train.business.resp.ConfirmOrderQueryResp;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
//import org.redisson.api.RLock;
//import org.redisson.api.RedissonClient;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.util.EnumUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ConfirmOrderServiceImpl extends ServiceImpl<ConfirmOrderMapper,ConfirmOrder> implements ConfirmOrderService {

    private final static String topic="tickets";


    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderServiceImpl.class);

    @Resource
    private ConfirmOrderMapper ConfirmOrdermapper;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private  DailyTrainTicketServiceImpl dailyTrainTicketService;

    @Resource
    private DailyTrainCarriageServiceImpl dailyTrainCarriageService;

    @Resource
    private DailyTrainSeatServiceImpl dailyTrainSeatService;

    @Resource
    private  AfterConfirmOrderServiceImpl afterConfirmOrderService;




    @Resource
    private RedissonClient redissonClient;

    public void save(ConfirmOrderDoReq req) {

        DateTime now = DateTime.now();
        ConfirmOrder ConfirmOrderm = BeanUtil.copyProperties(req, ConfirmOrder.class);
        if (ObjectUtil.isNull(ConfirmOrderm.getId())) {
            ConfirmOrderm.setId(ShowUtil.getSnowflakeNextId());
            ConfirmOrderm.setCreateTime(now);
            ConfirmOrderm.setUpdateTime(now);
            ConfirmOrdermapper.insert(ConfirmOrderm);
        } else {
            ConfirmOrderm.setUpdateTime(now);
            ConfirmOrdermapper.updateById(ConfirmOrderm);
        }
    }

    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req) {
    LambdaQueryWrapper<ConfirmOrder> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(ConfirmOrder::getId);

            LOG.info("查询页码：{}", req.getPage());
            LOG.info("每页条数：{}", req.getSize());
            PageHelper.startPage(req.getPage(), req.getSize());
            List<ConfirmOrder> selectedList = ConfirmOrdermapper.selectList(lambdaQueryWrapper);
            PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(selectedList);
                LOG.info("总行数：{}", pageInfo.getTotal());
                LOG.info("总页数：{}", pageInfo.getPages());

                List<ConfirmOrderQueryResp> list = BeanUtil.copyToList(selectedList, ConfirmOrderQueryResp.class);

                    PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
                        pageResp.setTotal(pageInfo.getTotal());
                        pageResp.setLists(list);
                        return pageResp;

    }



    public void delete(Long id) {
        ConfirmOrdermapper.deleteById(id);
    }


//    @SentinelResource("doConfirm")
    //@SentinelResource(value = "doConfirm",blockHandler = "doConfirmBlock")
    public void doConfirm(ConfirmOrderMQDto dto) throws InterruptedException {
       //省略业务数据校验，比如：车次是否存在。车次是否在有效期内。tickets条数>0,同车乘客是否已经买过票
        //令牌来进行限流操作
//        boolean validated = skTokenService.ValidateSKToken(req.getDate(), req.getTrainCode(), LoginMemberContext.getId());
//        if(!validated)
//        {
//            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SK_TOKEN_FAIL);
//        }
//        String key= LockKeyPreEnum.REDISSON_LOCK.getCode()+"-"+req.getDate()+"-"+req.getTrainCode();
//        RLock lock=null;
            //beforeConfirmOrderService.doBeforeComfirmOrder(req);
            String key= LockKeyPreEnum.REDISSON_LOCK.getCode()+"-"+dto.getDate()+"-"+dto.getTrainCode();
            RLock lock = redissonClient.getLock(key);
            //令牌算法进行限流
            boolean tried = lock.tryLock(0, TimeUnit.SECONDS);
            if(tried)
            {
                LOG.info("恭喜,抢到锁了,可以开始购票");
            }
            else{
                LOG.info("没有抢到锁，有其他消费线程正在出票，不做任何事情");
                doConfirm(dto);
                //return;
            }
            while (true)
            {
                QueryWrapper<ConfirmOrder> queryWrapper=new QueryWrapper<>();
                queryWrapper.eq("date",dto.getDate())
                        .eq("train_code",dto.getTrainCode())
                        .eq("status",ConfirmOrderStatusEnum.INIT.getCode())
                        .orderByAsc("id");
                PageHelper.startPage(1,5);
                List<ConfirmOrder> lists = ConfirmOrdermapper.selectList(queryWrapper);
                if(CollUtil.isNotEmpty(lists))
                {
                    LOG.info("没有需要处理的订单，结束循环");
                    break;
                }
                else{
                    LOG.info("本次处理{}条订单",lists.size());
                }
                lists.forEach(confirmOrder -> {
                    try {
                        //为会员增加购票记录
                        //更新确认订单为成功
                        sell(confirmOrder);
                    }catch (BusinessException e)
                    {
                        if(e.getAnEnum().equals(BusinessExceptionEnum.CONFIRM_TicketNumbers_Error))
                        {
                            LOG.info("本订单余票不足，继续售卖下一个订单");
                            confirmOrder.setStatus(ConfirmOrderStatusEnum.EMPTY.getCode());
                            updateStatus(confirmOrder);
                        }else{
                            throw e;
                        }
                    }
                });
            }

            LOG.info("购票流程结束，释放锁");
            if(lock.isHeldByCurrentThread())
            {
                lock.unlock();
            }


    }
    public void  sell(ConfirmOrder confirmOrder)
    {
        ConfirmOrderDoReq req = new ConfirmOrderDoReq();
        req.setMemberId(confirmOrder.getMemberId());
        req.setDate(confirmOrder.getDate());
        req.setTrainCode(confirmOrder.getTrainCode());
        req.setStart(confirmOrder.getStart());
        req.setEnd(confirmOrder.getEnd());
        req.setDailyTrainTicketId(confirmOrder.getDailyTrainTicketId());
        req.setTickets(JSON.parseArray(confirmOrder.getTickets(),ConfirmOrderTicketReq.class));

        LOG.info("更新订单状态");
        confirmOrder.setStatus(ConfirmOrderStatusEnum.PENDING.getCode());
        updateStatus(confirmOrder);


//        //从数据库里面来查询
//        QueryWrapper<ConfirmOrder> queryWrapper=new QueryWrapper<>();
//        queryWrapper.eq("date",req.getDate())
//                .eq("train_code",req.getTrainCode())
//                .eq("member_id",req.getMemberId())
//                .eq("status",ConfirmOrderStatusEnum.INIT.getCode());
//        List<ConfirmOrder> lists = ConfirmOrdermapper.selectList(queryWrapper);
//        if(CollUtil.isNotEmpty(lists))
//        {
//            LOG.info("找不到原始订单，结束");
//            return;
//        }
//        LOG.info("本次处理{}条订单",lists.size());


        Date date = req.getDate();
        String key= CacheEnum.Ticket_Nums.getCode()+"-"+req.getDate()+"-"+req.getTrainCode()+"-"+req.getStart()+"-"+req.getEnd();
        List<ConfirmOrderTicketReq> tickets = req.getTickets();
        //查出余票信息，得到真实的余票数据(预构建，来进行判断)
        DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.selectByUnique(req.getDate(), req.getTrainCode(), req.getStart(), req.getEnd());
        LOG.info("查出余票信息：{}", dailyTrainTicket);
        //扣减余票数量，并且判断余票是否足够
        reduceTickets(req, dailyTrainTicket);
        //删除余票的缓存信息

        List<DailyTrainSeat> finalSeatList = new ArrayList<>();
        //选座
        //            {
//                passengerId: 123,
//                passengerType:"1",
//                passengerName: "张三",
//                passengerIdCard:"1561461",
//                seatTypeCode:"1",
//                seat: "C1"
//            }
        ConfirmOrderTicketReq ticketReq0 = tickets.get(0);
        String trianCode=req.getTrainCode();
        if (StrUtil.isNotBlank(ticketReq0.getSeat())) {
            LOG.info("本次购票有选座");
            List<SeatColEnum> colEnumList = SeatColEnum.getColsByType(ticketReq0.getSeatTypeCode());
            LOG.info("本次选座的座位包括的座位的列：{}", colEnumList);
            //组成和前面两排一样的列表，用于做参找的座位列表，比如：referSeatList= {A1,C1,D1,F1,A2,C2,D2,F2}
            List<String> referSeatList = new ArrayList<>();
            for (int i = 1; i <= 2; i++) {
                for (SeatColEnum seatColEnum : colEnumList) {
                    referSeatList.add(seatColEnum.getCode() + i);
                }
            }
            LOG.info("用于做参考的两排座位：{}", referSeatList);
            //计算偏移值，即在参考座位列表中的位置
            List<Integer> OffsetList = new ArrayList<>();
            int init_index = referSeatList.indexOf(tickets.get(0).getSeat());
            for (ConfirmOrderTicketReq ticketReq : tickets) {
                int index = referSeatList.indexOf(ticketReq.getSeat()) - init_index;
                OffsetList.add(index);
            }
            LOG.info("计算得到的所有座位的相对第一个座位的相对偏移值：{}", OffsetList);
            getSeat(finalSeatList, date, trianCode,
                    ticketReq0.getSeatTypeCode(),
                    ticketReq0.getSeat().split("")[0],
                    OffsetList, dailyTrainTicket.getStartIndex(), dailyTrainTicket.getEndIndex());
        } else {
            LOG.info("本次购票没有选座");
            for (ConfirmOrderTicketReq ticketReq : tickets) {
                getSeat(finalSeatList, date, trianCode, ticketReq.getSeatTypeCode(), null, null, dailyTrainTicket.getStartIndex(), dailyTrainTicket.getEndIndex());
            }
        }
        LOG.info("最终选座：{}", finalSeatList);
        //选座完成后事务处理：
        //座位表修改售卖情况sell的数值； //余票详情表修改余票
        try {
            afterConfirmOrderService.afterDoFirm(dailyTrainTicket, finalSeatList, tickets,confirmOrder);
        } catch (Exception e) {
            LOG.error("保存购票信息失败：", e);
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_EXCEPTION);
        }

    }

    public void updateStatus(ConfirmOrder confirmOrder)
    {
        ConfirmOrder order = new ConfirmOrder();
        order.setId(confirmOrder.getId());
        order.setStatus(confirmOrder.getStatus());
        order.setUpdateTime(new Date());
        ConfirmOrdermapper.updateById(order);
    }
    //查询前面有几人在排队中
    public Integer queryLineCount(Long id)
    {
        ConfirmOrder confirmOrder = ConfirmOrdermapper.selectById(id);
        ConfirmOrderStatusEnum d=null;
        for (ConfirmOrderStatusEnum statusEnum:ConfirmOrderStatusEnum.values()) {
            if(statusEnum.getCode().equals(confirmOrder.getStatus()))
            {
                d=statusEnum;
                break;
            }
        }
        int result=switch (d){
            case PENDING -> 0;   //     排队0
            case SUCCESS -> -1; // 成功
            case FAILURE -> -2;// 失败
            case EMPTY -> -3;   //无票
            case CANCEL -> -4;  //取消
            case INIT -> 999;//需要查表得到实际排队数量
        };
        if (result==999)
        {
            QueryWrapper<ConfirmOrder>queryWrapper=new QueryWrapper<>();
            queryWrapper.or()
                    .eq("date",confirmOrder.getDate())
                    .eq("train_code",confirmOrder.getTrainCode())
                    .eq("status",ConfirmOrderStatusEnum.INIT.getCode())
                    .le("create_time",confirmOrder.getCreateTime());
            queryWrapper.or()
                    .eq("date",confirmOrder.getDate())
                    .eq("train_code",confirmOrder.getTrainCode())
                    .eq("status",ConfirmOrderStatusEnum.PENDING.getCode())
                    .le("create_time",confirmOrder.getCreateTime());
            Long count = ConfirmOrdermapper.selectCount(queryWrapper);
            return Math.toIntExact(count);
        }else {
            return  result;
        }

    }

    private void  getSeat(List<DailyTrainSeat> finalSeatList,Date date,String trainCode,String seatType,String column,List<Integer> offSetList,Integer startIndex,
                          Integer endIndex)
    {
        List<DailyTrainSeat> getSeatList=finalSeatList;
        List<DailyTrainCarriage> dailyTrainCarriageList = dailyTrainCarriageService.selectBySeatType(date, trainCode, seatType);
        LOG.info("共查出{}个符合条件的车厢",dailyTrainCarriageList.size());
        //一个车厢一个车厢的获取座位信息
        for (DailyTrainCarriage dailyTrainCarriage:dailyTrainCarriageList)
        {
            LOG.info("开始从车厢{}选座",dailyTrainCarriage.getIndexes());
            List<DailyTrainSeat> dailyTrainSeatList = dailyTrainSeatService.selectByCarriage(date, trainCode, dailyTrainCarriage.getIndexes());
            LOG.info("车厢{}的座位数为：{}",dailyTrainCarriage.getIndexes(),dailyTrainSeatList.size());
            for (int i=0;i<dailyTrainSeatList.size();i++)
            {
                DailyTrainSeat dailyTrainSeat = dailyTrainSeatList.get(i);
                //判断当前座位不能被选中过
                boolean alreadyChooseFlag=false;
                for (DailyTrainSeat finalSeat: finalSeatList)
                {
                    if(finalSeat.getId().equals(dailyTrainSeat.getId()))
                    {
                        alreadyChooseFlag=true;
                        break;
                    }
                }
                if(alreadyChooseFlag)
                {
                    LOG.info("座位{}被选中过，不能重复选中，继续判断下一个座位",seatType);
                    continue;
                }
                //判断column，有值的话需要进行比较
                String col = dailyTrainSeat.getCol();
                Integer seatIndex = dailyTrainSeat.getCarriageSeatIndex();
                if(StrUtil.isBlank(column))
                {
                    LOG.info("未选座");
                }
                else
                {
                    if(!column.equals(col))
                    {
                        LOG.info("座位{}的列值不符合要求，当前的座位列值：{}，乘客想要的座位列值：{}",
                                seatIndex,col,column
                        );
                        continue;
                    }
                }
                boolean isChoose = canSell(dailyTrainSeat, startIndex, endIndex);
                if(isChoose)
                {
                    LOG.info("选中座位");
                    getSeatList.add(dailyTrainSeat);
                }
                else {
                    continue;
                }
                //根据offSet选剩下的座位
                boolean isGetAllOffsetSeat=true;
                if (CollUtil.isNotEmpty(offSetList))
                {
                    LOG.info("有偏移值：{}，校验当前的座位是否可以选",offSetList);
                    //从索引1开始，索引0就是当前已经选中的票
                    for (int j=1;j<offSetList.size();j++)
                    {
                        Integer offset=offSetList.get(j);
                        int nextIndex=i+offset;
                        if(nextIndex>=dailyTrainSeatList.size())
                        {
                            LOG.info("座位{}不可选，偏移后的索引超出了这个车厢的座位数",nextIndex);
                            isGetAllOffsetSeat=false;
                            break;
                        }
                        DailyTrainSeat nextdailyTrainSeat = dailyTrainSeatList.get(nextIndex);
                        boolean isChooseNext = canSell(nextdailyTrainSeat, startIndex, endIndex);
                        if(isChooseNext)
                        {
                            LOG.info("座位{}被选中",nextdailyTrainSeat.getCarriageSeatIndex());
                            getSeatList.add(nextdailyTrainSeat);
                        }
                        else {
                            LOG.info("座位{}不可被选中",nextdailyTrainSeat.getCarriageSeatIndex());
                            isGetAllOffsetSeat=false;
                            break;
                        }
                    }
                }
                if (!isGetAllOffsetSeat)
                {
                    getSeatList.clear();
                    continue;
                }
                finalSeatList.addAll(getSeatList);
                //保存选好的座位
                return;
            }
        }
    }

     /**
     * 计算某个座位在区间是否可卖
     * 比如：sell=10001，代表第0到第1站已经卖了，第4到第5站可卖,本次购买区间为1-4，则截取出来为000
     * 某个区间全部是0，表示该区间可卖，只要有1，就表示区间内已卖过票
     * **/
    private  boolean  canSell(DailyTrainSeat dailyTrainSeat,Integer startIndex,Integer endIndex)  //计算座位在某个区间是否可卖
    {
            //10001 ,此时startIndex=1，endIndex=4
            String sell=dailyTrainSeat.getSell();
            String sellPart=sell.substring(startIndex,endIndex);//000
            if(Integer.parseInt(sellPart)>0)
            {
                LOG.info("座位{}在本次车站区间{}-{}已售过票，不可选中该座位",dailyTrainSeat.getCarriageSeatIndex(),
                        startIndex,endIndex);
                return  false;
            }else{
                LOG.info("座位{}在本次车站区间{}-{}未售过票，可以选中该座位",dailyTrainSeat.getCarriageSeatIndex(),
                        startIndex,endIndex);
                String curSell=sellPart.replace('0','1'); // 111
                curSell = StrUtil.fillBefore(curSell, '0', endIndex); // 0111
                curSell=StrUtil.fillBefore(curSell,'0',sell.length());//01110

                //当前区间的售票信息为curSell 01110和之前数据库里面的sell 00001按位于，即可得到该座位卖出此票后的售票情况
                // 15(01111),14=(01110=01110 | 00000)
                int newSellInt= NumberUtil.binaryToInt(curSell) | NumberUtil.binaryToInt(sell);
                String newSell=NumberUtil.getBinaryStr(newSellInt);
                newSell = StrUtil.fillBefore(newSell, '0', sell.length());
                LOG.info("座位{}被选中，原售票信息：{}，车次区间“{}-{}，即：{}，最终的售票信息：{}",
                        dailyTrainSeat.getCarriageSeatIndex(),sell,startIndex,endIndex,curSell,newSell);
                dailyTrainSeat.setSell(newSell);
                return  true;
            }
    }
    private void reduceTickets(ConfirmOrderDoReq req, DailyTrainTicket dailyTrainTicket) {
        for (ConfirmOrderTicketReq ticketReq :req.getTickets())
        {
            String seatTypeCode = ticketReq.getSeatTypeCode();
            SeatTypeEnum seatTypeEnum = SeatTypeEnum.getEnumByCode(seatTypeCode);
            switch (seatTypeEnum)
            {
                case YDZ -> {
                    int countLeft = dailyTrainTicket.getYdz()-1;
                    if(countLeft<0)
                    {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_TicketNumbers_Error);
                    }
                    dailyTrainTicket.setYdz(countLeft);
                }
                case EDZ -> {
                    int countLeft = dailyTrainTicket.getEdz()-1;
                    if(countLeft<0)
                    {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_TicketNumbers_Error);
                    }
                    dailyTrainTicket.setEdz(countLeft);
                }
                case RW -> {
                    int countLeft = dailyTrainTicket.getRw()-1;
                    if(countLeft<0)
                    {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_TicketNumbers_Error);
                    }
                    dailyTrainTicket.setRw(countLeft);
                }
                case YW -> {
                    int countLeft = dailyTrainTicket.getYw()-1;
                    if(countLeft<0)
                    {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_TicketNumbers_Error);
                    }
                    dailyTrainTicket.setYw(countLeft);
                }
            }
        }
    }




}

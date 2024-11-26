package com.java.train.member.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.java.train.common.req.MemberTicketReq;
import com.java.train.common.resp.PageResp;
import com.java.train.common.util.ShowUtil;
import com.java.train.member.entity.Ticket;
import com.java.train.member.mapper.TicketMapper;
import com.java.train.member.req.TicketQueryReq;
import com.java.train.member.req.TicketSaveReq;
import com.java.train.member.resp.TicketQueryResp;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper,Ticket> implements TicketService {



    private static final Logger LOG = LoggerFactory.getLogger(TicketServiceImpl.class);


    @Resource
    private TicketMapper Ticketmapper;




    public void save(MemberTicketReq req) {
        //LOG.info("seata全局事务ID save:{}", RootContext.getXID());
        DateTime now = DateTime.now();
        Ticket ticket = BeanUtil.copyProperties(req, Ticket.class);
        ticket.setId(ShowUtil.getSnowflakeNextId());
        ticket.setCreateTime(now);
        ticket.setUpdateTime(now);
        Ticketmapper.insert(ticket);
    }

    public PageResp<TicketQueryResp> queryList(TicketQueryReq req) {
        QueryWrapper<Ticket> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if(ObjectUtil.isNotEmpty((req.getMemberId())))
        {
            queryWrapper.eq("member_id",req.getMemberId());
        }
            LOG.info("查询页码：{}", req.getPage());
            LOG.info("每页条数：{}", req.getSize());
            PageHelper.startPage(req.getPage(), req.getSize());
            List<Ticket> selectedList = Ticketmapper.selectList(queryWrapper);
            PageInfo<Ticket> pageInfo = new PageInfo<>(selectedList);
                LOG.info("总行数：{}", pageInfo.getTotal());
                LOG.info("总页数：{}", pageInfo.getPages());

                List<TicketQueryResp> list = BeanUtil.copyToList(selectedList, TicketQueryResp.class);

                    PageResp<TicketQueryResp> pageResp = new PageResp<>();
                        pageResp.setTotal(pageInfo.getTotal());
                        pageResp.setLists(list);
                        return pageResp;

    }



    public void delete(Long id) {
        Ticketmapper.deleteById(id);
    }
}

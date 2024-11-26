package com.java.train.member.controller;

import cn.hutool.log.Log;
import com.java.train.common.context.LoginMemberContext;
import com.java.train.common.resp.CommonResp;
import com.java.train.common.resp.PageResp;
import com.java.train.member.req.TicketQueryReq;
import com.java.train.member.resp.TicketQueryResp;
import com.java.train.member.service.TicketServiceImpl;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ticket")
public class TicketController {


    @Resource
    private TicketServiceImpl ticketService;


    @PostMapping("/query-list")
    public  CommonResp<PageResp<TicketQueryResp >> query(@Valid TicketQueryReq req)
    {
        CommonResp<PageResp<TicketQueryResp >> commonResp=new CommonResp<>();
        req.setMemberId(LoginMemberContext.getId());
        PageResp<TicketQueryResp> PageResp = ticketService.queryList(req);
        commonResp.setContent(PageResp);
        return commonResp;
    }
}

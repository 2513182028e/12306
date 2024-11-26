package com.java.train.member.controller.feign;


import com.java.train.common.req.MemberTicketReq;
import com.java.train.common.resp.CommonResp;
import com.java.train.member.service.TicketServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feign/ticket")
public class FeignTicketController {



    @Autowired
    private TicketServiceImpl ticketService;

    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody MemberTicketReq req)
    {
        ticketService.save(req);
        return new CommonResp<>();
    }


}

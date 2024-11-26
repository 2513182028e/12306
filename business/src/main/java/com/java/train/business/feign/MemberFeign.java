package com.java.train.business.feign;


import com.java.train.common.req.MemberTicketReq;
import com.java.train.common.resp.CommonResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "member",url = "http://127.0.0.1:8002")
public interface MemberFeign {


    @GetMapping("/member/feign/ticket/save")
    CommonResp<Object> save(@RequestBody MemberTicketReq req);



}

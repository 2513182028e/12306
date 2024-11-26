package com.java.train.member.req;

import com.java.train.common.req.PageReq;
import lombok.Data;


public class TicketQueryReq extends PageReq {


    private  Long memberId;


    public  void  setMemberId(Long memberId)
    {
        this.memberId=memberId;
    }
    public Long getMemberId()
    {
        return memberId;
    }


}

package com.java.train.business.entity;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
public class ConfirmOrder implements Serializable {

    private Long id;

    private Long memberId;

    private Date date;

    private String trainCode;

    private String start;

    private String end;

    private Long dailyTrainTicketId;

    private String tickets;

    private String status;

    private Date createTime;

    private Date updateTime;


}

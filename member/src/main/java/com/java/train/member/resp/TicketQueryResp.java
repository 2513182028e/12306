package com.java.train.member.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class TicketQueryResp {

    private Long id;

    private Long memberId;
    private Long passengerId;
    @NotBlank(message = "【姓名】不能为空")
    private String passengerName;
    @NotBlank(message = "【日期】不能为空")
    private Date date;
    @NotBlank(message = "【火车编号】不能为空")
    private String trainCode;
    private Integer carriageIndex;
    private String row;
    private String col;
    private String start;
    private Date startTime;
    private String end;
    private Date endTime;
    private String seatType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;
}

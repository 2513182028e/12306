package com.java.train.common.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.util.Date;

@Data
public class MemberTicketReq {

    @NotNull(message = "【会员id】不能为空")
    private Long memberId;
    @NotNull(message = "【乘客id】不能为空")
    private Long passengerId;
    @NotNull(message = "【会员姓名】不能为空")
    private String passengerName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date seatDate;
    private String trainCode;
    private Integer carriageIndex;
    private String seatRow;
    private String seatCol;
    private String startStation;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date startTime;
    private String endStation;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date endTime;
    private String seatType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;
}

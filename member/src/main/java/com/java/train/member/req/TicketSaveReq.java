package com.java.train.member.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class TicketSaveReq {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long memberId;
    @JsonSerialize(using = ToStringSerializer.class)
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date startTime;
    private String end;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date endTime;
    @NotBlank(message = "【座位类型】不能为空")
    private String seatType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;
}

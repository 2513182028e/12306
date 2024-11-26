package com.java.train.member.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@TableName("ticket")
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {
    private Long id;
    private Long memberId;
    private Long passengerId;
    private String passengerName;
    private Date date;
    private String trainCode;
    private Integer carriageIndex;
    private String row;
    private String col;
    private String start;
    private Date startTime;
    private String end;
    private Date endTime;
    private String seatType;
    private Date createTime;
    private Date updateTime;


}

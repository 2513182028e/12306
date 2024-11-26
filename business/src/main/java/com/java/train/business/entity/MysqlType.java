package com.java.train.business.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MysqlType {

    private String id;

    private String date;

    private String trainCode;

    private String start;

    private String startPinyin;

    private String startTime;

    private String startIndex;

    private String end;

    private String endPinyin;

    private String endTime;

    private String endIndex;

    private String ydz;

    private  String ydzPrice;

    private String edz;

    private String edzPrice;

    private String rw;

    private String rwPrice;

    private String yw;

    private String ywPrice;

    private String createTime;

    private String updateTime;
}

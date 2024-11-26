package com.java.train.business.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlType {

    private int id;

    private int date;

    private int trainCode;

    private int start;

    private int startPinyin;

    private int startTime;

    private int startIndex;

    private int end;

    private int endPinyin;

    private int endTime;

    private int endIndex;

    private int ydz;

    private  int ydzPrice;

    private int edz;

    private int edzPrice;

    private int rw;

    private int rwPrice;

    private int yw;

    private int ywPrice;

    private int createTime;

    private int updateTime;
}

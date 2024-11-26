package com.java.train.business.entity;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;


@Data
@ToString
public class TrainCarriage implements Serializable {
    private Long id;

    private String trainCode;

    private Integer indexes;

    private String seatType;

    private Integer seatCount;

    private Integer rowCount;  // 该车厢的行数（数量）

    private Integer columnCount; // 该车厢的列数（数量）

    private Date createTime;

    private Date updateTime;

}
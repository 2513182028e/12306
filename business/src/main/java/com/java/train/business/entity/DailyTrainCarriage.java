package com.java.train.business.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
@TableName("daily_train_carriage")
public class DailyTrainCarriage implements Serializable {
    @TableId("id")
    private Long id;

    private Date date;

    private String trainCode;

    private Integer indexes;

    private String seatType;

    private Integer seatCount;

    private Integer rowCount;

    private Integer columnCount;

    private Date createTime;

    private Date updateTime;


}
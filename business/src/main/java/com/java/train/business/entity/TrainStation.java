package com.java.train.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ToString
@TableName("train_station")
public class TrainStation  implements Serializable {

    @TableId("id")
    private Long id;


    private String trainCode;


    private Integer indexes;


    private String name;


    private String namePinyin;


    private Date inTime;


    private Date outTime;

    private Date stopTime;


    private BigDecimal km;

    private Date createTime;

    private Date updateTime;

}
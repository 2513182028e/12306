package com.java.train.business.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sk_token")
public class SkToken {

    @TableId("id")
    private  Long id;
    private  Date date;
    private String trainCode;
    private  int count;
    private Date createTime;
    private Date updateTime;


}

package com.java.train.business.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfirmOrderMQDto {


    private String logId;

    private Date date;

    private String trainCode;


}

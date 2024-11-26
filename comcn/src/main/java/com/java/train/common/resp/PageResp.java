package com.java.train.common.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class PageResp<T> implements Serializable {


  private Long total;

    private List<T> lists;


  public PageResp(Long total, List<T> lists) {
    this.total = total;
    this.lists = lists;
  }

  public PageResp() {

  }
}

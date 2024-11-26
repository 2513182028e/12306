package com.java.train.business.enums;


public enum CacheEnum {


    Ticket_Nums("Tickets","余票缓存");

    private String code;
    private String desc;

    CacheEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

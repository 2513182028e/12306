package com.java.train.business.enums;




public enum LockKeyPreEnum{

    CONFIRM_ORDER("LOCK CONFIRM ORDER","一等座"),

    SK_TOKEN("LOCK_SK_TOKEN","二等座"),

    STREAM_LOCK("LOCK_STREAM","令牌锁"),

    SK_TOKEN_COUNT("LOCK_TOKEN_COUNT","令牌数量锁"),

    REDISSON_LOCK("LOCK_REDISSON","分布式锁");
    private final  String code;
    private final String desc;

    LockKeyPreEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

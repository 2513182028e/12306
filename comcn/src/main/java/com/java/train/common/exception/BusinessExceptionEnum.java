package com.java.train.common.exception;


public enum BusinessExceptionEnum {
    MEMBER_MOBILE_EXIST("手机号已注册"),
    MEMBER_MOBILE_NOT_EXIST("请先获取短信验证码"),

    TICKET_NUMS_ERROR("查询的车票不存在"),
    MEMBER_MOBILE_CODE_ERROR("短信验证码错误"),
    BUSINESS_STATION_NAME_UNIQUE_ERROR("车站已存在"),
    BUSINESS_TRAIN_CODE_UNIQUE_ERROR("车次编号已存在"),
    BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR("同车次站序已存在"),
    BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR("同车次站名已存在"),
    BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR("同车次厢号已存在"),

    CONFIRM_TicketNumbers_Error("余票数量不足") ,
    CONFIRM_ORDER_EXCEPTION("服务器繁忙，请稍后再试"),

    CONFIRM_ORDER_LOCK_FALI("当前抢票人数多，请稍后再试"),
    CONFIRM_OREDER_FLOW_EXCEPTION("当前抢票人数多，请稍后再试"),

    CONFIRM_ORDER_SK_TOKEN_FAIL("当前抢票人数多,获取令牌失败，请稍后再试");
    private String desc;

    BusinessExceptionEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "BusinessExceptionEnum{" +
                "desc='" + desc + '\'' +
                "} " + super.toString();
    }

}

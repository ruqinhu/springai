package com.example.shoppingguide.domain;

/**
 * 意图类型枚举
 */
public enum IntentType {
    SHOPPING("用户希望了解商品信息、配置、价格等导购服务"),
    ORDER("用户希望查询订单或获取售后服务"),
    GENERAL("一般性问候与寒暄"),
    UNKNOWN("无法识别的意图");

    private final String description;

    IntentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

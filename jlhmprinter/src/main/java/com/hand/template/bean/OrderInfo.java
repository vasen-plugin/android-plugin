package com.hand.template.bean;

public class OrderInfo extends TemplateInfo {
    private String orderId;
    private String zhiNum;
    private String miNum;
    private String weight;
    private String code;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getZhiNum() {
        return zhiNum;
    }

    public void setZhiNum(String zhiNum) {
        this.zhiNum = zhiNum;
    }

    public String getMiNum() {
        return miNum;
    }

    public void setMiNum(String miNum) {
        this.miNum = miNum;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

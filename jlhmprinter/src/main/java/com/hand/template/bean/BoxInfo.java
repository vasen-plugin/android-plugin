package com.hand.template.bean;

public class BoxInfo extends TemplateInfo{
    private String boxId;
    private String boxBarCode;

    public String getBoxId() {
        return boxId;
    }

    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }

    public String getBoxBarCode() {
        return boxBarCode;
    }

    public void setBoxBarCode(String boxBarCode) {
        this.boxBarCode = boxBarCode;
    }
}

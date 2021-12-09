package com.hand.template.bean;

public class MaterialInfo {

    private String materialId;//物料编码
    private String materialDes;//物料描述
    private String qrcode;
    private String model;//型号
    private String supplier;//供应商
    private String weldingWirePatch;//焊丝批次
    private String weldingWireHeatNum;//焊丝炉号
    private String purchaseOrderId;//采购订单编码

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public String getMaterialDes() {
        return materialDes;
    }

    public void setMaterialDes(String materialDes) {
        this.materialDes = materialDes;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getWeldingWirePatch() {
        return weldingWirePatch;
    }

    public void setWeldingWirePatch(String weldingWirePatch) {
        this.weldingWirePatch = weldingWirePatch;
    }

    public String getWeldingWireHeatNum() {
        return weldingWireHeatNum;
    }

    public void setWeldingWireHeatNum(String weldingWireHeatNum) {
        this.weldingWireHeatNum = weldingWireHeatNum;
    }

    public String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(String purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }
}

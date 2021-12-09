package com.hand.template;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.hand.baselibrary.config.Hippius;
import com.hand.baselibrary.utils.LogUtils;
import com.hand.jlhmprinter.R;
import com.hand.template.bean.BoxInfo;
import com.hand.template.bean.OrderInfo;

import org.json.JSONObject;

import cpcl.PrinterHelper;

public class OrderTemplatePrinter extends TemplatePrinter {

    private OrderInfo mOrderInfo;
    private Bitmap logo;

    public OrderTemplatePrinter() {
        logo = BitmapFactory
                .decodeResource(Hippius.getApplicationContext().getResources(), R.drawable.jl_logo);
    }

    private void initInfo(JSONObject object) {
        try {
            mOrderInfo = getGson().fromJson(object.toString(), OrderInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void print(JSONObject object) {
        initInfo(object);
        LogUtils.e("PRINT_OrderInfo", getGson().toJson(mOrderInfo));
        try {
            PrinterHelper.printAreaSize("0", "200", "200", "800", "1");
            PrinterHelper.printBitmapCPCL(logo, 460, 40, 0, 0, 0);
            /*PrinterHelper.Line("440", "0", "440", "800", "1");//最长的横线
            PrinterHelper.Line("540", "400", "440", "400", "1");//中间竖线*/
            PrinterHelper.SetBold("1");
            PrinterHelper.SetMag("2", "2");
            PrinterHelper.Text(PrinterHelper.TEXT270, "20", "0", "490", "280", "销售订单号:" + mOrderInfo.getOrderId());
            String otherDes = "支数：" + mOrderInfo.getZhiNum() + "       米数：" + mOrderInfo.getMiNum() + "       重量：" + mOrderInfo.getWeight();
            PrinterHelper.Text(PrinterHelper.TEXT270, "20", "0", "410", "50", otherDes);
            PrinterHelper.Barcode(PrinterHelper.VBARCODE, PrinterHelper.code128, "2", "2", "215", "100", "650", false, "7", "0", "5", mOrderInfo.getCode());
            //PrinterHelper.Line("100", "0", "100", "800", "1");//最长的横线
            PrinterHelper.Text(PrinterHelper.TEXT270, "20", "0", "75", "280", mOrderInfo.getCode());
            PrinterHelper.Form();
            PrinterHelper.Print();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
